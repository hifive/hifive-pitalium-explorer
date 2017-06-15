(function() {
	'use strict';

	/**
	 * @class hifive.pitalium.explorer.newDiff.PageController
	 */
	/**
	 * @lends hifive.pitalium.explorer.newDiff.PageController#
	 */
	var PageController = {
		/**
		 * @ignore
		 */
		__name: 'hifive.pitalium.explorer.newDiff.PageController',
		/**
		 * @ignore
		 */
		__templates: 'src/new_diff/new_diff.ejs',

		/**
		 * @type {hifive.pitalium.explorer.newDiff.PageLogic}
		 */
		_pageLogic: hifive.pitalium.explorer.newDiff.PageLogic,

		/**
		 * @type {JQuery}
		 */
		_$resultBox: null,
		/**
		 * @type {JQuery}
		 */
		_$expectedBox: null,
		/**
		 * @type {JQuery}
		 */
		_$opacityRange: null,
		/**
		 * @type {JQuery}
		 */
		_$memoBox: null,

		__ready: function() {
			this.log.debug('ready');

			this._$resultBox = this.$find('#result_box');
			this._$expectedBox = this.$find('#expected_box');
			this._$opacityRange = this.$find('#opacity_range');
			this._$memoBox = this.$find('#memobox');

			this._$resultBox.kinetic();
			this._$expectedBox.kinetic();

			this._initialize();
		},

		'#result_box scroll': function() {
			this._$expectedBox.scrollLeft(this._$resultBox.scrollLeft());
		},

		'#expected_box scroll': function() {
			this._$resultBox.scrollLeft(this._$expectedBox.scrollLeft());
		},

		'.diffDiv mouseenter': function(context, $el) {
			var rectype = $el.data("rectype");
			var method3 = $el.data("method3");
			var xshift = $el.data("xshift");
			var yshift = $el.data("yshift");
			var featurematrix = $el.data("featurematrix");
			var pixelbypixel = $el.data("pixelbypixel");
			var thres = $el.data("thres");
			var total = $el.data("total");

			var data1 = {
				type: rectype,
				xshift: xshift,
				yshift: yshift,
				featurematrix: featurematrix,
				pixelbypixel: pixelbypixel,
				thres: thres,
				total: total
			};

			this.view.update(this._$memoBox, 'memo_content', data1); //memo content update

			this._$memoBox.show();

			var opacity = parseFloat($el.css("opacity"));
			var index = $el.data("index");
			this.$find(".diff_index_" + index).css("opacity", (opacity + 0.3));
		},

		'.diffDiv mouseleave': function(context, $el) {
			this._$memoBox.hide();

			var opacity = parseFloat($el.css("opacity"));
			var index = $el.data("index");
			this.$find(".diff_index_" + index).css("opacity", (opacity - 0.3));
		},

		'{rootElement} mousemove': function(context) {
			var event = context.event;
			this._$memoBox.css({
				left: event.pageX + 10,
				top: event.pageY - 20
			});
		},

		'.platform mouseenver': function(context, $el) {
			$el.find("span").show();
		},

		'.platform mouseleave': function(context, $el) {
			$el.find("img").show();
			$el.find("span").hide();
		},

		_initialize: function() {

			var queryParams = hifive.pitalium.explorer.utils.getParameters(); //get parameters

			var expected = queryParams.expected; //expected image path
			var target = queryParams.target; //target image path

			var $imageLabels = this.$find('.image-label');
			$imageLabels.filter('.expected').text(expected.split('/')[2]);
			$imageLabels.filter('.result').text(target.split('/')[2]);

			var resultListId = queryParams.list_id; //result list id
			var targetResultId = queryParams.result_id; //result_id in result list
			var directory = queryParams.directory; //result directory
			var offsetX = 0 - parseInt(queryParams.offsetx); //dominant offset x
			var offsetY = 0 - parseInt(queryParams.offsety); //dominant offset y

			var offsetExpected = queryParams.offset_expected.toLowerCase() === "true";

			var minSimilarity = parseFloat(queryParams.minsimilarity); //this is for Importance control bar. It decides the importance control's minimum value.

			this.$find("#importance").attr("min", minSimilarity);
			this.$find('input[type="range"]').rangeslider();


			this.$find("#diffDetail").modal("show");
			/**
			 * @type {JQuery}
			 */
			var $testResult = this.$find('#test_result');
			$testResult
				.height('300')
				.width('100%')
				.css('background', '#fff url("res/img/big_loading.gif") center no-repeat');

			/**
			 * @type {JQuery}
			 */
			var $testExpected = this.$find('#test_expected');
			$testExpected
				.height('300')
				.width('100%')
				.css('background', '#fff url("res/img/big_loading.gif") center no-repeat');

			this.$find('.diffDiv').remove();

			this._pageLogic.fetchScreenshotImages(target, expected).done(this.own(function(data) {
				var offsets = offsetX + 'px ' + offsetY + 'px';
				var actual = document.createElement('img');
				actual.onload = function() {
					$testResult
						.height(this.height)
						.width(this.width)
						.data("width", this.width)
						.data("height", this.height)
						.css("background-image", "url('" + this.src + "')");

					if (!offsetExpected) {
						$testResult.css("background-position", offsets);
					}
				};

				actual.src = 'data:image/png;base64,' + data.targetImage;

				var expected = document.createElement('img');
				expected.onload = function() {
					$testExpected
						.height(this.height)
						.width(this.width)
						.data("width", this.width)
						.data("height", this.height)
						.css("background-image", "url('" + this.src + "')");

					if (offsetExpected) {
						$testExpected.css("background-position", offsets);
					}
				};
				expected.src = 'data:image/png;base64,' + data.expectedImage;

				this._drawRec(directory, resultListId, targetResultId); //this function draws all of red rectangles(divs)
			}));
		},

		_drawRec: function(directory, resultId, targetResultId) { //this function draws all of red rectangles(divs)
			this._pageLogic.fetchCompareResults(directory, resultId, targetResultId).done(this.own(function(data) {
				this.log.debug(data);

				for (var i = 0; i < data.length; i++) {

					var rec = data[i];
					var my_data;
					var s_unit = rec["similarityUnit"];

					if (rec["type"] == "SHIFT") {
						my_data = {
							x: rec["x"],
							y: rec["y"],
							width: rec["width"],
							height: rec["height"],
							type: rec["type"],
							xshift: rec["xshift"],
							yshift: rec["yshift"],
							featureMatrix: "",
							pixelByPixel: "1",
							thresDiff: "",
							totalDiff: "",
							index: i
						};

					} else {
						my_data = {
							x: rec["x"],
							y: rec["y"],
							width: rec["width"],
							height: rec["height"],
							type: rec["type"],
							xshift: rec["xshift"],
							yshift: rec["yshift"],
							featureMatrix: s_unit["similarityFeatureMatrix"],
							pixelByPixel: s_unit["similarityPixelByPixel"],
							thresDiff: s_unit["similarityThresDiff"],
							totalDiff: s_unit["similarityTotalDiff"],
							index: i
						};
					}

					this.view.append("#test_expected", "diffDiv", my_data);
					this.view.append("#test_result", "diffDiv", my_data);
				}
			}));
		},

		'.result_info click': function(context, $el) {
			$el.parent().find(".info_table_div").slideToggle();
		},

		'#opacity_range input': function() {
			this._onOpacityRangeChanged();
		},

		'#opacity_range change': function() {
			this._onOpacityRangeChanged();
		},

		_onOpacityRangeChanged: function() {
			this.$find(".diffDiv").css("opacity", this._$opacityRange.val());
			this.$find("#radio_none").trigger("click");
		},

		'#importance input': function(context, $el) {
			this._onImportanceChanged(context, $el);
		},

		'#importance change': function(context, $el) {
			this._onImportanceChanged(context, $el);
		},

		_onImportanceChanged: function(context, $el) {
			var value = $el.val();
			this.$find(".diffDiv").each(function() {
				var thisDiv = $(this);
				if ($("#checkbox_" + thisDiv.data("rectype")).is(":checked")) {
					if (thisDiv.data("pixelbypixel") > value) {
						thisDiv.hide();
					} else {
						thisDiv.show();
					}
				}
			});
		},

		'#img_size input change': function(context, $el) {
			this._onImageSizeChanged(context, $el);
		},

		'#img_size change': function(context, $el) {
			this._onImageSizeChanged(context, $el);
		},

		_onImageSizeChanged: function(context, $el) {
			var ratio = $el.val();

			$(".screenshot").each(function() {
				var $this = $(this);
				var this_height = $this.data("height");
				var this_width = $this.data("width");
				$this.height(this_height * ratio);
				$this.width(this_width * ratio);
				$this.css("background-size", String(this_width * ratio) + "px " + String(this_height * ratio) + "px");
			});

			$(".diffDiv").each(function() {
				var $this = $(this);
				var this_width = $this.data("width");
				var this_height = $this.data("height");
				var this_x = $this.data("x");
				var this_y = $this.data("y");

				$this.height(this_height * ratio);
				$this.width(this_width * ratio);

				$this.css("left", this_x * ratio);
				$this.css("top", this_y * ratio);
			});
		},

		'.choice change': function(context, $el) {
			var rectype = $el.data("rectype");
			var importance = parseFloat(this.$find("#importance").val());

			this.$find(".diff_" + rectype).each(function() {
				var $this = $(this);
				if (parseFloat($this.data("pixelbypixel")) <= importance) {
					$this.toggle();
				}
			});
		},

		'.click_prevent click': function(context) {
			context.event.stopPropagation();
		}

	};

	h5.core.expose(PageController);

})();