package dottytags.predefs

import scala.annotation.targetName

import dottytags._

/*
 * Documentation marked "MDN" is thanks to Mozilla Contributors
 * at https://developer.mozilla.org/en-US/docs/Web/API and available
 * under the Creative Commons Attribution-ShareAlike v2.5 or later.
 * http://creativecommons.org/licenses/by-sa/2.5/
 *
 * Everything else is under the MIT License, see here:
 * https://github.com/CiaraOBrien/dottytags/blob/main/LICENSE
 * 
 * This whole file is, of course, adapted from scalatags (see LICENSE for copyright notice):
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/generic/SvgTags.scala
 * https://github.com/lihaoyi/scalatags/blob/master/scalatags/src/scalatags/generic/SvgAttrs.scala
 */

object svg {

  inline def altGlyph = tag("altGlyph")
  inline def altGlyphDef = tag("altGlyphDef")
  inline def altGlyphItem = tag("altGlyphItem")
  inline def animate = tag("animate")
  inline def animateMotion = tag("animateMotion")
  inline def animateTransform = tag("animateTransform")
  inline def circle = tag("circle")
  inline def clipPath = tag("clipPath")
  inline def `color-profile` = tag("color-profile")
  //inline def cursorTag = tag("cursor") Deprecated and unsupported in many browsers, use cursor attribute instead
  inline def defs = tag("defs")
  inline def desc = tag("desc")
  inline def ellipse = tag("ellipse")
  inline def feBlend = tag("feBlend")
  inline def feColorMatrix = tag("feColorMatrix")
  inline def feComponentTransfer = tag("feComponentTransfer")
  inline def feComposite = tag("feComposite")
  inline def feConvolveMatrix = tag("feConvolveMatrix")
  inline def feDiffuseLighting = tag("feDiffuseLighting")
  inline def feDisplacementMap = tag("feDisplacementMap")
  inline def feDistantLighting = tag("feDistantLighting")
  inline def feFlood = tag("feFlood")
  inline def feFuncA = tag("feFuncA")
  inline def feFuncB = tag("feFuncB")
  inline def feFuncG = tag("feFuncG")
  inline def feFuncR = tag("feFuncR")
  inline def feGaussianBlur = tag("feGaussianBlur")
  inline def feImage = tag("feImage")
  inline def feMerge = tag("feMerge")
  inline def feMergeNode = tag("feMergeNode")
  inline def feMorphology = tag("feMorphology")
  inline def feOffset = tag("feOffset")
  inline def fePointLight = tag("fePointLight")
  inline def feSpecularLighting = tag("feSpecularLighting")
  inline def feSpotlight = tag("feSpotlight")
  inline def feTile = tag("feTile")
  inline def feTurbulance = tag("feTurbulance")
  inline def filter = tag("filter")
  inline def font = tag("font")
  inline def `font-face` = tag("font-face")
  inline def `font-face-format` = tag("font-face-format")
  inline def `font-face-name` = tag("font-face-name")
  inline def `font-face-src` = tag("font-face-src")
  inline def `font-face-uri` = tag("font-face-uri")
  inline def foreignObject = tag("foreignObject")
  inline def g = tag("g")
  inline def glyph = tag("glyph")
  inline def glyphRef = tag("glyphRef")
  inline def hkern = tag("hkern")
  inline def image = tag("image")
  inline def line = tag("line")
  inline def linearGradient = tag("linearGradient")
  inline def marker = tag("marker")
  inline def mask = tag("mask")
  inline def metadata = tag("metadata")
  //inline def missingGlyph = tag("missing-glyph") Not supported in literally any browser apparently
  inline def mpath = tag("mpath")
  inline def path = tag("path")
  inline def pattern = tag("pattern")
  inline def polygon = tag("polygon")
  inline def polyline = tag("polyline")
  inline def radialGradient = tag("radialGradient")
  inline def rect = tag("rect")
  inline def set = tag("set")
  inline def stop = tag("stop")
  inline def svg = tag("svg")
  inline def switch = tag("switch")
  inline def symbol = tag("symbol")
  inline def text = tag("text")
  inline def textPath = tag("textPath")
  inline def tref = tag("tref")
  inline def tspan = tag("tspan")
  inline def use = tag("use")
  inline def view = tag("view")
  inline def vkern = tag("vkern")

  /**
   * This attribute defines the distance from the origin to the top of accent characters,
   * measured by a distance within the font coordinate system.
   * If the attribute is not specified, the effect is as if the attribute
   * were set to the value of the ascent attribute.
   *
   * Value 	<number>
   *
   * MDN
   */
  inline def accentHeight = attr("accent-height")

  /**
   * This attribute controls whether or not the animation is cumulative.
   * It is frequently useful for repeated animations to build upon the previous results,
   * accumulating with each iteration. This attribute said to the animation if the value is added to
   * the previous animated attribute's value on each iteration.
   *
   * Value 	none | sum
   *
   * MDN
   */
  inline def accumulate = attr("accumulate")

  /**
   * This attribute controls whether or not the animation is additive.
   * It is frequently useful to define animation as an offset or delta
   * to an attribute's value, rather than as absolute values. This
   * attribute said to the animation if their values are added to the
   * original animated attribute's value.
   *
   * Value 	replace | sum
   *
   * MDN
   */
  inline def additive = attr("additive")

  /**
   * The alignment-baseline attribute specifies how an object is aligned
   * with respect to its parent. This property specifies which baseline
   * of this element is to be aligned with the corresponding baseline of
   * the parent. For example, this allows alphabetic baselines in Roman
   * text to stay aligned across font size changes. It defaults to the
   * baseline with the same name as the computed value of the
   * alignment-baseline property. As a presentation attribute, it also
   * can be used as a property directly inside a CSS stylesheet, see css
   * alignment-baseline for further information.
   *
   * Value: 	auto | baseline | before-edge | text-before-edge | middle | central | after-edge |
   * text-after-edge | ideographic | alphabetic | hanging | mathematical | inherit
   *
   * MDN
   */
  inline def alignmentBaseline = attr("alignment-baseline")


  /**
   * This attribute defines the maximum unaccented depth of the font
   * within the font coordinate system. If the attribute is not specified,
   * the effect is as if the attribute were set to the vert-origin-y value
   * for the corresponding font.
   *
   * Value 	<number>
   *
   * MDN
   */
  inline def ascent = attr("ascent")


  /**
   * This attribute indicates the name of the attribute in the parent element
   * that is going to be changed during an animation.
   *
   * Value 	<attributeName>
   *
   * MDN
   */
  inline def attributeName = attr("attributeName")


  /**
   * This attribute specifies the namespace in which the target attribute
   * and its associated values are defined.
   *
   * Value 	CSS | XML | auto
   *
   * MDN
   */
  inline def attributeType = attr("attributeType")


  /**
   * The azimuth attribute represent the direction angle for the light
   * source on the XY plane (clockwise), in degrees from the x axis.
   * If the attribute is not specified, then the effect is as if a
   * value of 0 were specified.
   *
   * Value 	<number>
   *
   * MDN
   */
  inline def azimuth = attr("azimuth")


  /**
   * The baseFrequency attribute represent The base frequencies parameter
   * for the noise function of the <feturbulence> primitive. If two <number>s
   * are provided, the first number represents a base frequency in the X
   * direction and the second value represents a base frequency in the Y direction.
   * If one number is provided, then that value is used for both X and Y.
   * Negative values are forbidden.
   * If the attribute is not specified, then the effect is as if a value
   * of 0 were specified.
   *
   * Value 	<number-optional-number>
   *
   * MDN
   */
  inline def baseFrequency = attr("baseFrequency")


  /**
   * The baseline-shift attribute allows repositioning of the dominant-baseline
   * relative to the dominant-baseline of the parent text content element.
   * The shifted object might be a sub- or superscript.
   * As a presentation attribute, it also can be used as a property directly
   * inside a CSS stylesheet, see css baseline-shift for further information.
   *
   * Value 	auto | baseline | sup | sub | <percentage> | <length> | inherit
   *
   * MDN
   */
  inline def baselineShift = attr("baseline-shift")


  /**
   * This attribute defines when an animation should begin.
   * The attribute value is a semicolon separated list of values. The interpretation
   * of a list of start times is detailed in the SMIL specification in "Evaluation
   * of begin and end time lists". Each individual value can be one of the following:
   * <offset-value>, <syncbase-value>, <event-value>, <repeat-value>, <accessKey-value>,
   * <wallclock-sync-value> or the keyword indefinite.
   *
   * Value 	<begin-value-list>
   *
   * MDN
   */
  inline def begin = attr("begin")


  /**
   * The bias attribute shifts the range of the filter. After applying the kernelMatrix
   * of the <feConvolveMatrix> element to the input image to yield a number and applied
   * the divisor attribute, the bias attribute is added to each component. This allows
   * representation of values that would otherwise be clamped to 0 or 1.
   * If bias is not specified, then the effect is as if a value of 0 were specified.
   *
   * Value 	<number>
   *
   * MDN
   */
  inline def bias = attr("bias")


  /**
   * This attribute specifies the interpolation mode for the animation. The default
   * mode is linear, however if the attribute does not support linear interpolation
   * (e.g. for strings), the calcMode attribute is ignored and discrete interpolation is used.
   *
   * Value 	discrete | linear | paced | spline
   *
   * MDN
   */
  inline def calcMode = attr("calcMode")


  /**
   * Assigns a class name or set of class names to an element. You may assign the same
   * class name or names to any number of elements. If you specify multiple class names,
   * they must be separated by whitespace characters.
   * The class name of an element has two key roles:
   * -As a style sheet selector, for use when an author wants to assign style
   * information to a set of elements.
   * -For general usage by the browser.
   * The class can be used to style SVG content using CSS.
   *
   * Value 	<list-of-class-names>
   *
   * MDN
   */
  inline def `class` = attr("class")


  /**
   * The clip attribute has the same parameter values as defined for the css clip property.
   * Unitless values, which indicate current user coordinates, are permitted on the coordinate
   * values on the <shape>. The value of auto defines a clipping path along the bounds of
   * the viewport created by the given element.
   * As a presentation attribute, it also can be used as a property directly inside a
   * CSS stylesheet, see css clip for further information.
   *
   * Value 	auto | <shape> | inherit
   *
   * MDN
   */
  inline def clip = attr("clip")


  /**
   * The clip-path attribute bind the element is applied to with a given <clipPath> element
   * As a presentation attribute, it also can be used as a property directly inside a CSS stylesheet
   *
   * Value 	<FuncIRI> | none | inherit
   *
   * MDN
   */
  inline def clipPathBind = attr("clip-path")

  /**
   * The clipPathUnits attribute defines the coordinate system for the contents
   * of the <clipPath> element. the clipPathUnits attribute is not specified,
   * then the effect is as if a value of userSpaceOnUse were specified.
   * Note that values defined as a percentage inside the content of the <clipPath>
   * are not affected by this attribute. It means that even if you set the value of
   * maskContentUnits to objectBoundingBox, percentage values will be calculated as
   * if the value of the attribute were userSpaceOnUse.
   *
   * Value 	userSpaceOnUse | objectBoundingBox
   *
   * MDN
   */
  inline def clipPathUnits = attr("clipPathUnits")

  /**
   * The clip-rule attribute only applies to graphics elements that are contained within a
   * <clipPath> element. The clip-rule attribute basically works as the fill-rule attribute,
   * except that it applies to <clipPath> definitions.
   *
   * Value 	nonezero | evenodd | inherit
   *
   * MDN
   */
  inline def clipRule = attr("clip-rule")

  /**
   * The color attribute is used to provide a potential indirect value (currentColor)
   * for the fill, stroke, stop-color, flood-color and lighting-color attributes.
   * As a presentation attribute, it also can be used as a property directly inside a CSS
   * stylesheet, see css color for further information.
   *
   * Value 	<color> | inherit
   *
   * MDN
   */
  inline def color = attr("color")


  /**
   * The color-interpolation attribute specifies the color space for gradient interpolations,
   * color animations and alpha compositing.When a child element is blended into a background,
   * the value of the color-interpolation attribute on the child determines the type of
   * blending, not the value of the color-interpolation on the parent. For gradients which
   * make use of the xlink:href attribute to reference another gradient, the gradient uses
   * the color-interpolation attribute value from the gradient element which is directly
   * referenced by the fill or stroke attribute. When animating colors, color interpolation
   * is performed according to the value of the color-interpolation attribute on the element
   * being animated.
   * As a presentation attribute, it also can be used as a property directly inside a CSS
   * stylesheet, see css color-interpolation for further information
   *
   * Value 	auto | sRGB | linearRGB | inherit
   *
   * MDN
   */
  inline def colorInterpolation = attr("color-interpolation")


  /**
   * The color-interpolation-filters attribute specifies the color space for imaging operations
   * performed via filter effects. Note that color-interpolation-filters has a different
   * initial value than color-interpolation. color-interpolation-filters has an initial
   * value of linearRGB, whereas color-interpolation has an initial value of sRGB. Thus,
   * in the default case, filter effects operations occur in the linearRGB color space,
   * whereas all other color interpolations occur by default in the sRGB color space.
   * As a presentation attribute, it also can be used as a property directly inside a
   * CSS stylesheet, see css color-interpolation-filters for further information
   *
   * Value 	auto | sRGB | linearRGB | inherit
   *
   * MDN
   */
  inline def colorInterpolationFilters = attr("color-interpolation-filters")


  /**
   * The color-profile attribute is used to define which color profile a raster image
   * included through the <image> element should use. As a presentation attribute, it
   * also can be used as a property directly inside a CSS stylesheet, see css color-profile
   * for further information.
   *
   * Value 	auto | sRGB | <name> | <IRI> | inherit
   *
   * MDN
   */
  inline def colorProfile = attr("color-profile")


  /**
   * The color-rendering attribute provides a hint to the SVG user agent about how to
   * optimize its color interpolation and compositing operations. color-rendering
   * takes precedence over color-interpolation-filters. For example, assume color-rendering:
   * optimizeSpeed and color-interpolation-filters: linearRGB. In this case, the SVG user
   * agent should perform color operations in a way that optimizes performance, which might
   * mean sacrificing the color interpolation precision as specified by
   * color-interpolation-filters: linearRGB.
   * As a presentation attribute, it also can be used as a property directly inside
   * a CSS stylesheet, see css color-rendering for further information
   *
   * Value 	auto | optimizeSpeed | optimizeQuality | inherit
   *
   * MDN
   */
  inline def colorRendering = attr("color-rendering")


  /**
   * The contentScriptType attribute on the <svg> element specifies the default scripting
   * language for the given document fragment.
   * This attribute sets the default scripting language used to process the value strings
   * in event attributes. This language must be used for all instances of script that do not
   * specify their own scripting language. The value content-type specifies a media type,
   * per MIME Part Two: Media Types [RFC2046]. The default value is application/ecmascript
   *
   * Value 	<content-type>
   *
   * MDN
   */
  inline def contentScriptType = attr("contentScriptType")


  /**
   * This attribute specifies the style sheet language for the given document fragment.
   * The contentStyleType is specified on the <svg> element. By default, if it's not defined,
   * the value is text/css
   *
   * Value 	<content-type>
   *
   * MDN
   */
  inline def contentStyleType = attr("contentStyleType")


  /**
   * The cursor attribute specifies the mouse cursor displayed when the mouse pointer
   * is over an element.This attribute behave exactly like the css cursor property except
   * that if the browser suport the <cursor> element, it should allow to use it with the
   * <funciri> notation. As a presentation attribute, it also can be used as a property
   * directly inside a CSS stylesheet, see css cursor for further information.
   *
   * Value 	 auto | crosshair | default | pointer | move | e-resize |
   * ne-resize | nw-resize | n-resize | se-resize | sw-resize | s-resize | w-resize| text |
   * wait | help | inherit
   *
   * MDN
   */
  inline def cursor = attr("cursor")


  /**
   * For the <circle> and the <ellipse> element, this attribute define the x-axis coordinate
   * of the center of the element. If the attribute is not specified, the effect is as if a
   * value of "0" were specified.For the <radialGradient> element, this attribute define
   * the x-axis coordinate of the largest (i.e., outermost) circle for the radial gradient.
   * The gradient will be drawn such that the 100% gradient stop is mapped to the perimeter
   * of this largest (i.e., outermost) circle. If the attribute is not specified, the effect
   * is as if a value of 50% were specified
   *
   * Value 	<coordinate>
   *
   * MDN
   */
  inline def cx = attr("cx")

  /**
   * For the <circle> and the <ellipse> element, this attribute define the y-axis coordinate
   * of the center of the element. If the attribute is not specified, the effect is as if a
   * value of "0" were specified.For the <radialGradient> element, this attribute define
   * the x-axis coordinate of the largest (i.e., outermost) circle for the radial gradient.
   * The gradient will be drawn such that the 100% gradient stop is mapped to the perimeter
   * of this largest (i.e., outermost) circle. If the attribute is not specified, the effect
   * is as if a value of 50% were specified
   *
   * Value 	<coordinate>
   *
   * MDN
   */
  inline def cy = attr("cy")


  /**
   *
   *
   * MDN
   */
  inline def d = attr("d")


  /**
   *
   *
   * MDN
   */
  inline def diffuseConstant = attr("diffuseConstant")


  /**
   *
   *
   * MDN
   */
  inline def direction = attr("direction")


  /**
   *
   *
   * MDN
   */
  inline def display = attr("display")


  /**
   *
   *
   * MDN
   */
  inline def divisor = attr("divisor")


  /**
   *
   *
   * MDN
   */
  inline def dominantBaseline = attr("dominant-baseline")


  /**
   *
   *
   * MDN
   */
  inline def dur = attr("dur")


  /**
   *
   *
   * MDN
   */
  inline def dx = attr("dx")


  /**
   *
   *
   * MDN
   */
  inline def dy = attr("dy")


  /**
   *
   *
   * MDN
   */
  inline def edgeMode = attr("edgeMode")


  /**
   *
   *
   * MDN
   */
  inline def elevation = attr("elevation")


  /**
   *
   *
   * MDN
   */
  inline def end = attr("end")


  /**
   *
   *
   * MDN
   */
  inline def externalResourcesRequired = attr("externalResourcesRequired")


  /**
   *
   *
   * MDN
   */
  inline def fill = attr("fill")


  /**
   *
   *
   * MDN
   */
  inline def fillOpacity = attr("fill-opacity")


  /**
   *
   *
   * MDN
   */
  inline def fillRule = attr("fill-rule")


  /**
   * The filter attribute specifies the filter effects defined by the `<filter>` element that shall be applied to its element.
   * 
   * MDN
   */
  inline def filterBind = attr("filter")


  /**
   *
   *
   * MDN
   */
  inline def filterRes = attr("filterRes")


  /**
   *
   *
   * MDN
   */
  inline def filterUnits = attr("filterUnits")


  /**
   *
   *
   * MDN
   */
  inline def floodColor = attr("flood-color")


  /**
   *
   *
   * MDN
   */
  inline def floodOpacity = attr("flood-opacity")


  /**
   *
   *
   * MDN
   */
  inline def fontFamily = attr("font-family")


  /**
   *
   *
   * MDN
   */
  inline def fontSize = attr("font-size")


  /**
   *
   *
   * MDN
   */
  inline def fontSizeAdjust = attr("font-size-adjust")


  /**
   *
   *
   * MDN
   */
  inline def fontStretch = attr("font-stretch")


  /**
   *
   *
   * MDN
   */
  inline def fontVariant = attr("font-variant")


  /**
   *
   *
   * MDN
   */
  inline def fontWeight = attr("font-weight")


  /**
   *
   *
   * MDN
   */
  inline def from = attr("from")


  /**
    *
    *
    * MDN
    */
  inline def fx = attr("fx")


  /**
    *
    *
    * MDN
    */
  inline def fy = attr("fy")

  
  /**
   *
   *
   * MDN
   */
  inline def gradientTransform = attr("gradientTransform")


  /**
   *
   *
   * MDN
   */
  inline def gradientUnits = attr("gradientUnits")


  /**
   *
   *
   * MDN
   */
  inline def height = attr("height")


  /**
   *
   *
   * MDN
   */
  inline def imageRendering = attr("imageRendering")

  inline def id = attr("id")

  /**
   *
   *
   * MDN
   */
  inline def in = attr("in")



  /**
   *
   *
   * MDN
   */
  inline def in2 = attr("in2")



  /**
   *
   *
   * MDN
   */
  inline def k1 = attr("k1")


  /**
   *
   *
   * MDN
   */
  inline def k2 = attr("k2")


  /**
   *
   *
   * MDN
   */
  inline def k3 = attr("k3")


  /**
   *
   *
   * MDN
   */
  inline def k4 = attr("k4")



  /**
   *
   *
   * MDN
   */
  inline def kernelMatrix = attr("kernelMatrix")



  /**
   *
   *
   * MDN
   */
  inline def kernelUnitLength = attr("kernelUnitLength")


  /**
   *
   *
   * MDN
   */
  inline def kerning = attr("kerning")


  /**
   *
   *
   * MDN
   */
  inline def keySplines = attr("keySplines")



  /**
   *
   *
   * MDN
   */
  inline def keyTimes = attr("keyTimes")




  /**
   *
   *
   * MDN
   */
  inline def letterSpacing = attr("letter-spacing")



  /**
   *
   *
   * MDN
   */
  inline def lightingColor = attr("lighting-color")



  /**
   *
   *
   * MDN
   */
  inline def limitingConeAngle = attr("limitingConeAngle")



  /**
   *
   *
   * MDN
   */
  inline def local = attr("local")



  /**
   *
   *
   * MDN
   */
  inline def markerEnd = attr("marker-end")


  /**
   *
   *
   * MDN
   */
  inline def markerMid = attr("marker-mid")


  /**
   *
   *
   * MDN
   */
  inline def markerStart = attr("marker-start")


  /**
   *
   *
   * MDN
   */
  inline def markerHeight = attr("markerHeight")


  /**
   *
   *
   * MDN
   */
  inline def markerUnits = attr("markerUnits")


  /**
   *
   *
   * MDN
   */
  inline def markerWidth = attr("markerWidth")


  /**
   *
   *
   * MDN
   */
  inline def maskContentUnits = attr("maskContentUnits")


  /**
   *
   *
   * MDN
   */
  inline def maskUnits = attr("maskUnits")


  /**
   *
   *
   * MDN
   */
  inline def maskBind = attr("mask")

  /**
   *
   *
   * MDN
   */
  inline def max = attr("max")



  /**
   *
   *
   * MDN
   */
  inline def min = attr("min")


  /**
   *
   *
   * MDN
   */
  inline def mode = attr("mode")


  /**
   *
   *
   * MDN
   */
  inline def numOctaves = attr("numOctaves")


  inline def offset = attr("offset")

  /**
    * The ‘orient’ attribute indicates how the marker is rotated when it is placed at its position on the markable element.
    *
    * W3C
    */
  inline def orient = attr("orient")

  /**
   *
   *
   * MDN
   */
  inline def opacity = attr("opacity")



  /**
   *
   *
   * MDN
   */
  inline def operator = attr("operator")


  /**
   *
   *
   * MDN
   */
  inline def order = attr("order")


  /**
   *
   *
   * MDN
   */
  inline def overflow = attr("overflow")



  /**
   *
   *
   * MDN
   */
  inline def paintOrder = attr("paint-order")



  /**
   *
   *
   * MDN
   */
  inline def pathLength = attr("pathLength")



  /**
   *
   *
   * MDN
   */
  inline def patternContentUnits = attr("patternContentUnits")


  /**
   *
   *
   * MDN
   */
  inline def patternTransform = attr("patternTransform")



  /**
   *
   *
   * MDN
   */
  inline def patternUnits = attr("patternUnits")



  /**
   *
   *
   * MDN
   */
  inline def pointerEvents = attr("pointer-events")


  /**
   *
   *
   * MDN
   */
  inline def points = attr("points")


  /**
   *
   *
   * MDN
   */
  inline def pointsAtX = attr("pointsAtX")


  /**
   *
   *
   * MDN
   */
  inline def pointsAtY = attr("pointsAtY")


  /**
   *
   *
   * MDN
   */
  inline def pointsAtZ = attr("pointsAtZ")


  /**
   *
   *
   * MDN
   */
  inline def preserveAlpha = attr("preserveAlpha")



  /**
   *
   *
   * MDN
   */
  inline def preserveAspectRatio = attr("preserveAspectRatio")



  /**
   *
   *
   * MDN
   */
  inline def primitiveUnits = attr("primitiveUnits")


  /**
   *
   *
   * MDN
   */
  inline def r = attr("r")



  /**
   *
   *
   * MDN
   */
  inline def radius = attr("radius")


  /**
    * The ‘refX’ attribute defines the reference point of the marker which is to be placed exactly at
    * the marker's position on the markable element. It is interpreted as being in the coordinate system of
    * the marker contents, after application of the ‘viewBox’ and ‘preserveAspectRatio’ attributes.
    *
    *  W3C
    */
  inline def refX = attr("refX")

  /**
    * The ‘refY’ attribute defines the reference point of the marker which is to be placed exactly at
    * the marker's position on the markable element. It is interpreted as being in the coordinate system of
    * the marker contents, after application of the ‘viewBox’ and ‘preserveAspectRatio’ attributes.
    *
    *  W3C
    */
  inline def refY = attr("refY")

  /**
   *
   *
   * MDN
   */
  inline def repeatCount = attr("repeatCount")


  /**
   *
   *
   * MDN
   */
  inline def repeatDur = attr("repeatDur")



  /**
   *
   *
   * MDN
   */
  inline def requiredFeatures = attr("requiredFeatures")



  /**
   *
   *
   * MDN
   */
  inline def restart = attr("restart")



  /**
   *
   *
   * MDN
   */
  inline def result = attr("result")



  /**
   *
   *
   * MDN
   */
  inline def rx = attr("rx")



  /**
   *
   *
   * MDN
   */
  inline def ry = attr("ry")



  /**
   *
   *
   * MDN
   */
  inline def scale = attr("scale")



  /**
   *
   *
   * MDN
   */
  inline def seed = attr("seed")



  /**
   *
   *
   * MDN
   */
  inline def shapeRendering = attr("shape-rendering")



  /**
   *
   *
   * MDN
   */
  inline def specularConstant = attr("specularConstant")



  /**
   *
   *
   * MDN
   */
  inline def specularExponent = attr("specularExponent")



  /**
   *
   *
   * MDN
   */
  inline def spreadMethod = attr("spreadMethod")



  /**
   *
   *
   * MDN
   */
  inline def stdDeviation = attr("stdDeviation")



  /**
   *
   *
   * MDN
   */
  inline def stitchTiles = attr("stitchTiles")



  /**
   *
   *
   * MDN
   */
  inline def stopColor = attr("stop-color")



  /**
   *
   *
   * MDN
   */
  inline def stopOpacity = attr("stop-opacity")



  /**
   *
   *
   * MDN
   */
  inline def stroke = attr("stroke")


  /**
   *
   *
   * MDN
   */
  inline def strokeDasharray= attr("stroke-dasharray")


  /**
   *
   *
   * MDN
   */
  inline def strokeDashoffset = attr("stroke-dashoffset")


  /**
   *
   *
   * MDN
   */
  inline def strokeLinecap = attr("stroke-linecap")


  /**
   *
   *
   * MDN
   */
  inline def strokeLinejoin = attr("stroke-linejoin")


  /**
   *
   *
   * MDN
   */
  inline def strokeMiterlimit = attr("stroke-miterlimit")


  /**
   *
   *
   * MDN
   */
  inline def strokeOpacity = attr("stroke-opacity")


  /**
   *
   *
   * MDN
   */
  inline def strokeWidth = attr("stroke-width")


  /**
   *
   *
   * MDN
   */
  inline def style = attr("style")



  /**
   *
   *
   * MDN
   */
  inline def surfaceScale = attr("surfaceScale")


  /**
   *
   *
   * MDN
   */
  inline def targetX = attr("targetX")


  /**
   *
   *
   * MDN
   */
  inline def targetY = attr("targetY")


  /**
   *
   *
   * MDN
   */
  inline def textAnchor = attr("text-anchor")


  /**
   *
   *
   * MDN
   */
  inline def textDecoration = attr("text-decoration")


  /**
   *
   *
   * MDN
   */
  inline def textRendering = attr("text-rendering")


  /**
   *
   *
   * MDN
   */
  inline def to = attr("to")


  /*
   *
   *
   * MDN
   */
  inline def transform = attr("transform")


  /*
   *
   *
   * MDN
   */
  inline def `type`= attr("type")


  /*
   *
   *
   * MDN
   */
  inline def values = attr("values")


  /**
   *
   *
   * MDN
   */
  inline def viewBox = attr("viewBox")


  /*
   *
   *
   * MDN
   */
  inline def visibility = attr("visibility")


  /*
   *
   *
   * MDN
   */
  inline def width = attr("width")


  /*
   *
   *
   * MDN
   */
  inline def wordSpacing = attr("word-spacing")

  /*
   *
   *
   * MDN
   */
  inline def writingMode = attr("writing-mode")


  /*
   *
   *
   * MDN
   */
  inline def x = attr("x")


  /*
   *
   *
   * MDN
   */
  inline def x1 = attr("x1")


  /*
   *
   *
   * MDN
   */
  inline def x2 = attr("x2")


  /*
   *
   *
   * MDN
   */
  inline def xChannelSelector = attr("xChannelSelector")


  /*
   *
   *
   * MDN
   */
  inline def xLinkHref= attr("xlink:href")


  /*
   *
   *
   * MDN
   */
  inline def xLink = attr("xlink:role")


  /*
   *
   *
   * MDN
   */
  inline def xLinkTitle = attr("xlink:title")


  /*
   *
   *
   * MDN
   */
  inline def xmlSpace = attr("xml:space")


  /**
   *
   *
   * MDN
   */
  inline def xmlns = attr("xmlns")


  /**
   *
   *
   * MDN
   */
  inline def xmlnsXlink = attr("xmlns:xlink")


  /*
   *
   *
   * MDN
   */
  inline def y = attr("y")


  /*
   *
   *
   * MDN
   */
  inline def y1 = attr("y1")


  /*
   *
   *
   * MDN
   */
  inline def y2 = attr("y2")


  /*
   *
   *
   * MDN
   */
  inline def yChannelSelector = attr("yChannelSelector")


  /*
   *
   *
   * MDN
   */
  inline def z = attr("z")
  
}