/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
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

		/** original title */
		_orgTitle: null,

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
		showResult: function(screenshot, expectedScreenshot) {
			this._imageLoadPromises = [];

			this._screenshot = screenshot;
			this._initializeImageSelector(screenshot.targets);
			return h5.async.when(this._imageLoadPromises).done(this.own(function() {
				this._triggerViewChange();
			}));
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
			var val = imageSelector.val();
			this._compareImages(val);
		},

		'.nav-tabs shown.bs.tab': function(context, $el) {
			this._triggerViewChange();
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
			this._compareImages(val);
		},

		_compareImages: function(targetId) {
			if (this._screenshot.expectedScreenshotId != null) {
				this._testResultDiffLogic.getComparisonResult(this._screenshot, targetId).done(
						this.own(function(comparisonResult) {
							this._screenshot.comparisonResult = comparisonResult;
							// Fire change event and show images.
							this._setImage(targetId);
							this.trigger('updateComparisonResult', {
								comparisonResult: comparisonResult
							});
						}));
			} else {
				this._screenshot.comparisonResult = null;
				// Fire change event and show images.
				this._setImage(targetId);
				this.trigger('updateComparisonResult', {
					comparisonResult: null
				});
			}


		},

		_setImage: function(targetId) {
			this._imageLoadPromises = [];
			var screenshotId = this._screenshot.id;

			var expectedScreenshotId = this._screenshot.expectedScreenshotId;
			// Expected mode
			if (expectedScreenshotId == null) {
				this._setActualImageSrc(false, {
					screenshotId: screenshotId,
					targetId: targetId
				});
				this._showExpectedMode();
				this._hideActualMode();
				h5.async.when(this._imageLoadPromises).done(this.own(function() {
					this._triggerViewChange();
				}));
				return;
			}
			this._showActualMode();
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
					sourceScreenshotId: screenshotId,
					targetScreenshotId: expectedScreenshotId,
					targetId: targetId
				});

				this._setExpectedImageSrc(true, {
					sourceScreenshotId: expectedScreenshotId,
					targetScreenshotId: screenshotId,
					targetId: targetId
				});
			}

			this._initEdgeOverlapping(expectedScreenshotId, screenshotId, targetId);
			h5.async.when(this._imageLoadPromises).done(this.own(function() {
				this._triggerViewChange();
			}));
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
			expected.src = format('image/processed', {
				screenshotId: expectedId,
				targetId: targetId,
				algorithm: 'edge',
				colorIndex: 1
			});
			actual.src = format('image/processed', {
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
					actualBlack.src = format('image/processed', {
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

			var url = withMarker ? 'image/diff' : 'image';
			this.$find(selector).attr('src', hifive.pitalium.explorer.utils.formatUrl(url, params));
			this.$find(selector)[0].onload = function() {
				dfd.resolve();
			};
			this._imageLoadPromises.push(dfd.promise());
		},

		/**
		 * Show actual mode.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_showActualMode: function() {
			this.$find('#actual-mode').show();
		},

		/**
		 * Show expected mode.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 */
		_showExpectedMode: function() {
			this.$find('#expected-mode').show();
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
		},

		_triggerViewChange: function() {
			this.trigger('viewChanged');
		}
	};

	h5.core.expose(testResultDiffController);
})(jQuery);