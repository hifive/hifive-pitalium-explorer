/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a &qout;logic&quot; for the test result comparison page.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.logic
	 * @name TestResultDiffLogic
	 */
	var testResultDiffLogic = {
		/**
		 * @memberOf hifive.pitalium.explorer.logic.TestResultDiffLogic
		 */
		__name: 'hifive.pitalium.explorer.logic.TestResultDiffLogic',

		/**
		 * Get details of the screenshot.
		 * 
		 * @memberOf hifive.pitalium.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the screenshot
		 * @returns {JqXHRWrapper}
		 */
		getScreenshot: function(id) {
			return h5.ajax({
				type: 'get',
				url: 'api/getScreenshot',
				data: {
					screenshotId: id
				}
			});
		},

		/**
		 * Get screenshots of the current execution and environment.
		 * 
		 * @memberOf hifive.pitalium.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the screenshot
		 * @returns {JqXHRWrapper}
		 */
		listScreenshot: function(executionId, environmentId) {
			var dfd = this.deferred();

			h5.ajax('api/listScreenshot', {
				data: {
					testExecutionId: executionId,
					testEnvironmentId: environmentId
				},
				type: 'GET',
				dataType: 'json'
			}).done(this.own(function(response) {
				var screenshots = response.content;
				var map = this._convertToTreeMap(screenshots);
				dfd.resolve(map);
			}));
			return dfd.promise();
		},

		_convertToTreeMap: function(screenshotArray) {
			var retMap = {};
			for (var i = 0, len = screenshotArray.length; i < len; i++) {
				var s = screenshotArray[i];
				var testClass = s.testClass;
				var testClassObj = retMap[testClass];
				if (!testClassObj) {
					testClassObj = {};
					retMap[testClass] = testClassObj;
				}

				var testMethod = s.testMethod;
				var screenshotsOfMethod = testClassObj[testMethod];
				if (!screenshotsOfMethod) {
					screenshotsOfMethod = [];
					testClassObj[testMethod] = screenshotsOfMethod;
				}

				screenshotsOfMethod.push(s);
			}
			return retMap;
		}
	};

	h5.core.expose(testResultDiffLogic);
})(jQuery);
(function($) {
	/**
	 * This class is a controller for the list of screeenshots.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name ScreenshotListController
	 */
	var screenshotListController = {
		__name: 'hifive.pitalium.explorer.controller.ScreenshotListController',

		/**
		 * The &quot;Logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.ScreenshotListController
		 */
		_testResultDiffLogic: hifive.pitalium.explorer.logic.TestResultDiffLogic,

		/**
		 * Show the list of screenshots.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ScreenshotListController
		 */
		showList: function(screenshot) {
			return this._testResultDiffLogic.listScreenshot(screenshot.testExecution.id,
					screenshot.testEnvironment.id).done(this.own(function(screenshotMap) {
				this._showList(screenshotMap);
			}));
		},

		_showList: function(screenshotMap) {
			var treeData = [];
			for ( var testClass in screenshotMap) {
				var children = [];
				var testMethodMap = screenshotMap[testClass];
				for ( var testMethod in testMethodMap) {
					var screenshots = testMethodMap[testMethod];
					var child = {
						text: testMethod,
						children: [],
						state: {
							opened: true
						}
					};
					children.push(child);

					for (var i = 0, len = screenshots.length; i < len; i++) {
						var s = screenshots[i];
						child.children.push({
							text: s.screenshotName,
							icon: false,
							a_attr: {
								'class': 'screenshot',
								'data-explorer-screenshot-id': s.id
							},
							state: {
								opened: true
							}
						});
					}
				}
				treeData.push({
					text: testClass,
					children: children,
					state: {
						opened: true
					}
				});
			}

			this.$find('#tree_root').jstree({
				'core': {
					data: treeData
				}
			});
		},

		'.screenshot click': function(context, $el) {
			var id = $el.data('explorerScreenshotId');
			this.trigger('selectScreenshot', {
				id: id
			});
		}
	};

	h5.core.expose(screenshotListController);

})(jQuery);

(function($) {
	/**
	 * This class is a controller for the test result comparison.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name TestResultDiffController
	 */
	var testResultDiffController = {
		/**
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		__name: 'hifive.pitalium.explorer.controller.TestResultDiffController',

		/**
		 * The &quot;Logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_testResultDiffLogic: hifive.pitalium.explorer.logic.TestResultDiffLogic,

		_screenshot: {},

		_imageLoadDeferred: null,

		/**
		 * Called after the controller has been initialized.<br>
		 * Get the id of the right screenshot, and init views.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		__ready: function() {
			this._initializeSwipeHandle();
			this._initializeOnionHandle();

			this.$find('#quick-flipping .image-diff.expected').css('opacity', 0.2);
			this.$find('#quick-flipping .image-overlay .expected').hide();
		},

		/**
		 * Show the result of the selected screenshot id.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		showResult: function(screenshot) {
			this._imageLoadPromises = [];

			this._screenshot = screenshot;
			this._initializeImageSelector(screenshot.targets);
			this.view.update('#detail', 'testResultListTemplate', {
				testResult: screenshot
			});
			return h5.async.when(this._imageLoadPromises);
		},

		/**
		 * Initialize the drop down of image selection.
		 * 
		 * @memberOf hifiveTestExplorer.controller.TestResultDiffController
		 * @param {Array} includes the selectors for the test area inclusion.
		 */
		_initializeImageSelector: function(targets) {
			this.view.update('#selector', 'imageSelectorTemplate', {
				targets: targets
			});
			// Generate select options
			var imageSelector = this.$find('#imageSelector');
			// Fire change event and show images.
			imageSelector.change();
		},

		/**
		 * Called when the selection of the drop down changed.<br>
		 * Update images.
		 * 
		 * @memberOf hifiveTestExplorer.controller.TestResultDiffController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#imageSelector change': function(context, $el) {
			var val = $el.val();
			this._setImage(val);
		},

		_setImage: function(targetId) {
			var screenshotId = this._screenshot.id;

			var expectedScreenshotId = this._screenshot.expectedScreenshotId;
			// Expected mode
			if (expectedScreenshotId == null) {
				this._setActualImageSrc(false, {
					screenshotId: screenshotId,
					targetId: targetId
				});
				this._hideActualMode();
				return;
			}
			this._hideExpectedMode();

			if (this._screenshot.comparisonResult) {
				// Test succeeded
				this._setActualImageSrc(false, {
					screenshotId: screenshotId,
					targetId: targetId
				});

				this._setExpectedImageSrc(false, {
					screenshotId: expectedScreenshotId,
					targetId: targetId
				});
			} else {
				// Test failed
				this._setActualImageSrc(true, {
					sourceSceenshotId: screenshotId,
					targetScreenshotId: expectedScreenshotId,
					targetId: targetId
				});

				this._setExpectedImageSrc(true, {
					sourceSceenshotId: expectedScreenshotId,
					targetScreenshotId: screenshotId,
					targetId: targetId
				});
			}

			this._initEdgeOverlapping(expectedScreenshotId, screenshotId, targetId);
		},

		'#quick-flipping .image-diff click': function(context, $el) {
			var $actual = this.$find('#quick-flipping .image-overlay .expected');
			if ($el.hasClass('expected')) {
				$actual.show();
			} else {
				$actual.hide();
			}
			$el.css('opacity', 1);
			$el.siblings().css('opacity', 0.2);
		},

		/**
		 * Show actual image.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 * @param {Boolean} withMarker whether or not to display the image with markers.
		 * @param {Object} params extra paramters
		 */
		_setActualImageSrc: function(withMarker, params) {
			this._setImageSrc('.actual img', withMarker, params);
		},

		/**
		 * Show expected image.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 * @param {Boolean} withMarker whether or not to display the image with markers.
		 * @param {Object} params extra paramters
		 */
		_setExpectedImageSrc: function(withMarker, params) {
			this._setImageSrc('.expected img', withMarker, params);
		},


		/**
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 * @param {Number} expectedId ID of expected image
		 * @param {Number} actualId ID of actual image
		 * @param {Number} targetId ID the target area to be used for image comparison
		 */
		_initEdgeOverlapping: function(expectedId, actualId, targetId) {
			// Initialize <canvas>
			var expected = new Image();
			var actual = new Image();

			var d1 = $.Deferred(),d2 = $.Deferred();
			expected.onload = d1.resolve;
			actual.onload = d2.resolve;

			var format = hifive.pitalium.explorer.utils.formatUrl;
			expected.src = format('image/getProcessed', {
				screenshotId: expectedId,
				targetId: targetId,
				algorithm: 'edge',
				colorIndex: 1
			});
			actual.src = format('image/getProcessed', {
				screenshotId: actualId,
				targetId: targetId,
				algorithm: 'edge',
				colorIndex: 0
			});

			$.when.apply($, [d1.promise(), d2.promise()]).done(this.own(function() {
				var canvas = this.$find('#edge-overlapping canvas')[0];
				var native_width = canvas.width = expected.width;
				var native_height = canvas.height = expected.height;

				var context = canvas.getContext('2d');
				context.globalCompositeOperation = 'multiply';
				if (context.globalCompositeOperation == 'multiply') {
					context.drawImage(expected, 0, 0);
					context.drawImage(actual, 0, 0);
					this._initImageMagnifier(native_width, native_height);
				} else {
					// IE workaround
					var actualBlack = new Image();
					actualBlack.onload = function() {
						context.drawImage(expected, 0, 0);
						context.globalCompositeOperation = 'source-atop';
						context.drawImage(actualBlack, 0, 0);
						context.globalCompositeOperation = 'destination-over';
						context.drawImage(actual, 0, 0);
						this._initImageMagnifier(native_width, native_height);
					};
					actualBlack.src = format('image/getProcessed', {
						screenshotId: actualId,
						targetId: targetId,
						algorithm: 'edge',
						colorIndex: 2
					});
				}
			}));
		},

		_initImageMagnifier: function(native_width, native_height) {
			// Image magnifier
			var canvas = this.$find('#edge-overlapping canvas')[0];
			var $large = this.$find('.large');
			var $small = this.$find('.small');
			$large.css('background-image', 'url(' + canvas.toDataURL('image/png') + ')');
			this.$find('#edge-overlapping .image-overlay').mousemove(
					function(e) {
						var magnify_offset = $(this).offset();
						var mx = e.pageX - magnify_offset.left;
						var my = e.pageY - magnify_offset.top;

						if (mx < $(this).width() && my < $(this).height() && mx > 0 && my > 0) {
							$large.fadeIn(100);
						} else {
							$large.fadeOut(100);
						}
						if ($large.is(':visible')) {
							var rx = Math.round(mx / $small.width() * native_width
									- $('.large').width() / 2)
									* -1;
							var ry = Math.round(my / $small.height() * native_height
									- $large.height() / 2)
									* -1;

							$large.css({
								left: mx - $large.width() / 2,
								top: my - $large.height() / 2,
								backgroundPosition: rx + 'px ' + ry + 'px'
							});
						}
					});
		},

		/**
		 * Show image.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 * @param {String} selector jQuery selector expression which determines the image node
		 * @param {Boolean} withMarker whether or not to display the image with markers.
		 * @param {Object} params extra paramters
		 */
		_setImageSrc: function(selector, withMarker, params) {
			var dfd = this.deferred();

			var url = withMarker ? 'image/getDiff' : 'image/get';
			this.$find(selector).attr('src', hifive.pitalium.explorer.utils.formatUrl(url, params));
			this.$find(selector)[0].onload = function() {
				dfd.resolve();
			};
			this._imageLoadPromises.push(dfd.promise());
		},

		/**
		 * Hide actual mode.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_hideActualMode: function() {
			this.$find('#actual-mode').hide();
		},

		/**
		 * Hide expected mode.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_hideExpectedMode: function() {
			this.$find('#expected-mode').hide();
		},

		/**
		 * Initialize the swipe diff handle.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_initializeSwipeHandle: function() {
			var min = 0,max = 1000,step = 1;

			var $handle = this.$find('#swipe-handle');
			var $actual = this.$find('#swipe .expected');
			var $actualImg = this.$find('#swipe .expected > img');

			$handle.attr('min', min);
			$handle.attr('max', max);
			$handle.attr('step', step);
			$handle.val(max);

			var inputHandler = function() {
				var val = $handle.val();
				var percentage = ((val - min) / (max - min) * 100);
				$actual.css('left', percentage + '%');
				$actualImg.css('margin-left', (-percentage) + '%');
			};
			$handle.on('input', inputHandler);
			$handle.on('change', inputHandler); // for IE

			inputHandler();
		},

		/**
		 * Initialize the onion skin diff handle.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_initializeOnionHandle: function() {
			var min = 0,max = 1000,step = 1;

			var $handle = this.$find('#onion-handle');
			var $actual = this.$find('#onion-skin .image-diff .expected');

			$handle.attr('min', min);
			$handle.attr('max', max);
			$handle.attr('step', step);
			$handle.val(max);

			var inputHandler = function() {
				var val = $handle.val();
				var ratio = (val - min) / (max - min);
				$actual.css('opacity', 1 - ratio);
			};
			$handle.on('input', inputHandler);
			$handle.on('change', inputHandler); // for IE

			inputHandler();
		}
	};

	h5.core.expose(testResultDiffController);
})(jQuery);

(function($) {
	/**
	 * This class is a controller for the test result diff page.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name DiffPageController
	 */
	var diffPageController = {
		__name: 'hifive.pitalium.explorer.controller.DiffPageController',

		/**
		 * The &quot;Logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.DiffPageController
		 */
		_testResultDiffLogic: hifive.pitalium.explorer.logic.TestResultDiffLogic,

		_dividedboxController: h5.ui.components.DividedBox.DividedBox,

		_screenshotListController: hifive.pitalium.explorer.controller.ScreenshotListController,

		_testResultDiffController: hifive.pitalium.explorer.controller.TestResultDiffController,

		/** original title */
		_orgTitle: null,

		__meta: {
			_screenshotListController: {
				rootElement: '#list'
			},
			_testResultDiffController: {
				rootElement: '#main'
			}
		},

		/**
		 * Called after the controller has been initialized.<br>
		 * Get the id of the right screenshot, and update views.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.DiffPageController
		 */
		__ready: function() {
			// Get the id of the test result from url query parameters.
			var queryParams = hifive.pitalium.explorer.utils.getParameters();
			if (!queryParams.hasOwnProperty('id')) {
				alert('ID not found');
				return;
			}

			var id = queryParams.id;

			// Get screenshot detailsT
			this._testResultDiffLogic.getScreenshot(id).done(this.own(function(screenshot) {
				this._changeTitle(screenshot.comparisonResult);

				var diffPromise = this._testResultDiffController.showResult(screenshot);
				var listPromise = this._screenshotListController.showList(screenshot);

				h5.async.when(diffPromise, listPromise).done(this.own(this._refreshView));
			}));
		},

		_refreshView: function() {
			var height = this.$find('#main')[0].scrollHeight;
			$(this.rootElement).height(height);
			this._dividedboxController.refresh();
		},

		_changeTitle: function(comparisonResult) {
			if (this._orgTitle == null) {
				this._$title = $('title');
				this._orgTitle = this._$title.text();
			}

			if (comparisonResult == null) {
				this._$title.text(this._orgTitle);
			} else if (comparisonResult) {
				this._$title.text('○ ' + this._orgTitle);
			} else {
				this._$title.text('× ' + this._orgTitle);
			}
		},

		'#list selectScreenshot': function(context, $el) {
			var id = context.evArg.id;
			this._testResultDiffLogic.getScreenshot(id).done(this.own(function(screenshot) {
				this._testResultDiffController.showResult(screenshot);
			}));
		},

		'{window} [resize]': function() {
			//			this._dividedboxController.refresh();
			this._refreshView();
		}
	};

	h5.core.expose(diffPageController);

})(jQuery);
$(function() {
	h5.core.controller('#container', hifive.pitalium.explorer.controller.DiffPageController);
});
