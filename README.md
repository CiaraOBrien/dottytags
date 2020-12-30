# DottyTags
An experimental reimplementation of [ScalaTags](http://www.lihaoyi.com/scalatags/) in (extremely meta) Scala 3. It is a more-or-less working clone of
ScalaTags from the user's perspective, with most of the surface syntax being nearly identical, but the internals are radically different, as Scala 3's 
metaprogramming capabilities are leveraged to automatically reduce the tree as much as possible to simple serial concatenation of strings at compile-time. 
Therefore, the code that actually runs is, in many cases, basially an array of string literals interspersed with string expressions that are evaluated at runtime 
and then appended with the literal spans in a single linear `StringBuilder` loop. Currently, `Tag` contents are eagerly evaluated, so it is not sound (as it were) 
to let extra attributes or styles be added post-hoc in any way. I intend to sort this out soon, but for the time being it's the state of affairs. This leads to the 
primary syntactic and semantic limitations of DottyTags relative to ScalaTags:

* First, this means no chained `Tag` applications like `div(cls := "header", backgroundColor := "blue")(divContents)` in ScalaTags. In my opinion they are pointless anyway,
  and even aesthetically unappealing, but they can't be implemented either way until tag evaluation is made lazier.
* Second, this means that any attributes or styles added to a tag will not combine with or overwrite previous ones of the same name as they do in ScalaTags,
  but again, repeated attributes and styles are arguably a pretty bad idea to have in your HTML in the first place. 
* Third, this means that fragments (`Frag`s) can only contain `Element`s -- that is, `Raw | Tag | Frag | String`, because otherwise a `Frag` which had to be computed at runtime
  could inject a bunch of `Attr`s and `Style`s into the tag's body splice, and there is currently no clear way (to me) of rearranging the spans in a splice post-hoc 
  while preserving intended ordering and that sort of thing. This effectively means that the number of attributes and styles  applied to a `Tag` (and, separately, 
  their names, though not their values) must be static, since `Frag`s are the only way a variable number of children can be inserted. This could be remedied 
  with a simple optional-modifier type that could add an `Attr`/`Style` or not depending on some condition, but it's not worth doing when I could just sort out the 
  evaluation and solve all these problems in one go.

## Performance
Based on my very unprofessional benchmark comparisons, DottyTags is around 3-4 times faster than ScalaTags when
the comparison is roughly fair (complex HTML trees involving loops, external variables, etc., like those used in ScalaTags'
own benchmarks), and up to 75 times faster in less-fair comparisons involving mostly-static tree generation, in which DottyTags has an absurd advantage
since entirely-static trees get flattened into single string literals at compile-time, and trees with only a few dynamic elements pretty much boil down to
an `Array[String]` being appended to a `StringBuilder`, where most of the elements of the array are string literals. This speed disparity carries over more or
less directly to Scala.JS, when comparing DottyTags to ScalaTags' text backend - its DOM backend is far, far slower.

## Example
As a quick example, this:
```scala
import dottytags._
import dottytags.utils.implicits.given
import scala.language.implicitConversions
import dottytags.utils.cssUnits._
import dottytags.predefs.tags._
import dottytags.predefs.attrs._
import dottytags.predefs.styles._
println(html(cls := "foo", href := "bar", css("baz1") := "qux", "quux", 
  System.currentTimeMillis.toString, css("baz2") := "qux", raw("a")
).render)
```
Boils down to something like:
```scala
println(Tag.apply(
  dottytags.splice(
    Array[String](
      "<html class=\"foo\" href=\"bar\" style=\"baz: qux;\">quux",
      dottytags.util.escape(scala.Long.box(System.currentTimeMillis()).toString()), 
      "a</html>"
    )
  )
).str())
```
Which, when run, yields:
```html
<html class="foo" href="bar" style="baz1: qux; baz2: qux;">quux1608810396295a</html>
```
For comparison, ScalaTags' interpretation of the same code (by swapping out the imports, since the syntax is broadly compatible in most cases):
```scala
scalatags.Text.all.html().asInstanceOf[scalatags.Text.Text$TypedTag].apply(
  scala.runtime.ScalaRunTime.wrapRefArray([
    scalatags.Text.all.cls().:=("foo",
      scalatags.Text.all.stringAttr()
    ),
    scalatags.Text.all.href().:=("bar",
      scalatags.Text.all.stringAttr()
    ),
    scalatags.Text.all.css("baz1").:=("qux",
      scalatags.Text.all.stringStyle()
    ),
    scalatags.Text.all.stringFrag("quux"),
    scalatags.Text.all.stringFrag(
      scala.Long.box(System.currentTimeMillis()).toString()
    ),
    scalatags.Text.all.css("baz2").:=("qux",
      scalatags.Text.all.stringStyle()
    ),
    scalatags.Text.all.raw("a") : scalatags.generic.Modifier
  ])
).render()
```

## Reflections
### On Micro-Optimization for Micro-Optimization's Sake
This architecture is pretty similar to how `s` works (or at least the Dotty implementation thereof), except that DottyTags does more work up-front rather than relying 
on the JVM to always generate efficient code for chained string concatenations. Of course, this hides the point that the JVM *almost always does* generate quite fast code 
out of even apparently-slow Scala code. Staring at the post-MegaPhase `dotc` output like I have been doing to make this library still doesn't really tell you how fast the
JVM will actually end up making the resulting bytecode (given warm-up time of course). So this quest for compile-time computation is somewhat quixotic, 
though as evidenced by the benchmarks above, it does have its benefits. These improvements can matter, especially in extreme cases where a massive amount of data 
has to be processed with very high throughput, though these cases usually involve parsing rather than building data structures.

### On the Library's Guts
However, a major advantage that DottyTags has over "stringy" solutions like XML interpolation is that DottyTags has a (relatively) type-safe API like ScalaTags',
but one that vanishes into the aether at runtime. This API is still something of a work in progress, and as such it has some limitations relative to ScalaTags' API, 
but in my experience so far, these limitations mostly manifest as an inability to do certain things which would probably be a performance hit anyway and could be done
more effectively in another way, which DottyTags usually supports just fine.

The internals (including the type system the API is based on) are ~~currently pretty~~ no longer nearly so cursed! if you dig into them, though they're much better than 
they were initially. I intend to rework the API's type system to make the internals more elegant (relative to its current state), enable more type safety in the API,
and free up some of the more arbitrary-feeling restrictions (again, relative to ScalaTags' API).

It should be feasible to support ScalaJS DOM, but I have no experience whatsoever with ScalaJS, or JS in general for that matter. Rather than implementing a generic
API frontend with different backends (string concatenation, JS DOM, and JVM Virtual DOM) like ScalaTags, it would likely be simplest to just port the core functionality
(namely the macros) wholesale to whatever other backends and then just copypaste the API.

### On Aetheric APIs
While Scala 3's metaprogramming lets you do amazing and elegant things on the client code side of the equation, it leads to a fair bit of boilerplate and repetition 
on the library side of things, since it's much harder to take advantage of traditional code reuse patterns when almost all of the API is meant to be inlined into thin air. 
Any method call or variable access that can't be either inlined or elided breaks the chain of macro introspection, and pretty much has to be treated as a 
runtime-only black box, so pretty much all API methods have to be either "fake" -- existing only to enforce type relationships in the typechecking phase and then to be 
elided as soon as the macros get their grubby mitts on them -- or `inline`, which poses several problems as `inline` is currently rather flaky in Scala 3, 
especially when it comes to overriding things, since any remotely dynamic use of `inline override def`s leads to the inliner throwing up its hands and either erroring 
out or silently falling back to an out-of-line method call, depending on whether the super-class `def` being overridden is `inline` or not.

While this API architecture is more or less type-safe, not in the least because you can inspect and enforce invariants with macros, IDE support is very much
not there yet (a statement that applies to Dotty as a whole). At the moment, any DottyTags call site is at least half the time riddled with erroneous errors that 
have no relation to anything going on during actual compilation because apparently the presentation compiler doesn't play well with macros yet. Additionally, there are 
some weird issues with the new regime of varargs ascription, which is why `bind(Seq[Element])` is separate from `frag(Element*)` for the time being (the former can now be
omitted in user code if you import the implicit conversions module).

## Vague Goals
- ✓ Make the internals more coherent by overhauling the core types (i.e. stop using my cursed "sticky note" paradigm and actually make some ADTs), which should also result in more elegance, safety, and extensibility
- This would involve relaxing the up-front staticity requirements (Turns out nope!), but we could possibly make up for it with smarter macros
- Switch to using `FromExpr` and `ToExpr` extractors more, they seem alright now that they're stabilized. I originally used lots of TASTy stuff, then I mostly switched to splice patterns since they're way nicer to work with.
- ✓ Sort out `frag` and `bind`, possibly by eliminating them. This might require implicit conversions though, and ~~I don't think it's possible at the moment to do inline implicit conversions based on previous experimentation.~~ it is definitely possible to do them inline but it's annoying .
- If whitebox macros become a thing, I might be able to use their ~~black~~ white magic to obviate `frag`/`bind`
- Add more type safety and convenience stuff to the builtins, especially CSS styles, e.g. numeric range enforcement (via macros), unit/datatype safety , colors, actual support for multi-styles like `border`
