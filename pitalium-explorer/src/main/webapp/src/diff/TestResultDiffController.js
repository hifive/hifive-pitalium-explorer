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

		_screenshot:  {
			'expected': null,
			'actual': null
		},
		_comparisonResult: null,

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
		showResult: function(screenshot) {
			this._imageLoadPromises = [];

			if (screenshot.actual) {
				this._screenshot = screenshot;
				this._initializeImageSelector(null);
			} else {
				var targetId = screenshot.targets[0].targetId;
				this._screenshot.actual = {
					'screenshotId': screenshot.id,
					'targetId': targetId
				};

				if (screenshot.expectedScreenshotId) {
					this._screenshot.expected = {
						'screenshotId': screenshot.expectedScreenshotId,
						'targetId': targetId
					};
				} else {
					this._screenshot.expected = null;
				}

				this._comparisonResult = screenshot.comparisonResult;
				this._initializeImageSelector(screenshot.targets);
			}

			return h5.async.when(this._imageLoadPromises).done(this.own(function() {
				this._triggerViewChange();
			}));
		},

		/**
		 * Initialize the drop down of image selection.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 * @param {Array} targets the selectors for the test area inclusion.
		 */
		_initializeImageSelector: function(targets) {
			// No targets... do not show selector
			if (targets === null) {
				this.$find('#selector').hide();
				this._compareImages(null);
				return;
			}

			this.$find('#selector').show();
			this.view.update('#selector', 'imageSelectorTemplate', {
				'targets': targets
			});

			// Generate select options
			var imageSelector = this.$find('#imageSelector');
			var val = imageSelector.val();
			this._compareImages(val);
		},

		'.nav-tabs shown.bs.tab': function() {
			this._triggerViewChange();
		},

		/**
		 * Called when the selection of the drop down changed.<br>
		 * Update images.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.TestResultDiffController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#imageSelector change': function(context, $el) {
			var val = $el.val();
			this._compareImages(val);
		},

		_compareImages: function(targetId) {
			if (targetId !== null) {
				this._screenshot.actual.targetId = targetId;
				if (this._screenshot.expected != null) {
					this._screenshot.expected.targetId = targetId;
				}
			}

			if (this._screenshot.expected != null) {
				this._testResultDiffLogic.getComparisonResult(this._screenshot).done(
						this.own(function(comparisonResult) {
							this._comparisonResult = comparisonResult;
							// Fire change event and show images.
							this._setImage();
							this.trigger('updateComparisonResult', {
								comparisonResult: comparisonResult
							});
						}));

				return;
			}

			this._comparisonResult = null;
			// Fire change event and show images.
			this._setImage();
			this.trigger('updateComparisonResult', {
				comparisonResult: null
			});
		},

		_setImage: function() {
			this._imageLoadPromises = [];
			var expected = this._screenshot.expected;
			var actual = this._screenshot.actual;

			// Expected mode
			if (expected == null) {
				this._setImageExpected(actual);
				return;
			}

			this._showActualMode();
			this._hideExpectedMode();

			if (this._comparisonResult) {
				// Test succeeded
				this._setActualImageSrc(false, actual);
				this._setExpectedImageSrc(false, expected);
			} else {
				// Test failed
				this._setActualImageSrc(true, {
					sourceScreenshotId: actual.screenshotId,
					targetScreenshotId: expected.screenshotId,
					sourceTargetId: actual.targetId,
					targetTargetId: expected.targetId
				});

				this._setExpectedImageSrc(true, {
					sourceScreenshotId: expected.screenshotId,
					targetScreenshotId: actual.screenshotId,
					sourceTargetId: expected.targetId,
					targetTargetId: actual.targetId
				});
			}

			this._initEdgeOverlapping(expected, actual);
			h5.async.when(this._imageLoadPromises).done(this.own(function() {
				this._triggerViewChange();
			}));
		},

		'_setImageExpected': function(screenshot) {
			this._setActualImageSrc(false, screenshot);
			this._showExpectedMode();
			this._hideActualMode();
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
		 * @param {object} expected object which contains ID of expected image and ID the target area to be used for image comparison
		 * @param {object} actual object which contains ID of actual image and ID the target area to be used for image comparison
		 */
		_initEdgeOverlapping: function(expected, actual) {
			// Initialize <canvas>
			var expectedImage = new Image();
			var actualImage = new Image();

			var d1 = $.Deferred(),d2 = $.Deferred();
			expectedImage.onload = d1.resolve;
			actualImage.onload = d2.resolve;

			var format = hifive.pitalium.explorer.utils.formatUrl;
			expectedImage.src = format('image/processed', {
				screenshotId: expected.screenshotId,
				targetId: expected.targetId,
				algorithm: 'edge',
				colorIndex: 1
			});
			actualImage.src = format('image/processed', {
				screenshotId: actual.screenshotId,
				targetId: actual.targetId,
				algorithm: 'edge',
				colorIndex: 0
			});

			$.when.apply($, [d1.promise(), d2.promise()]).done(this.own(function() {
				var canvas = this.$find('#edge-overlapping canvas')[0];
				var native_width = canvas.width = expectedImage.width;
				var native_height = canvas.height = expectedImage.height;

				var context = canvas.getContext('2d');
				context.globalCompositeOperation = 'multiply';
				if (context.globalCompositeOperation == 'multiply') {
					context.drawImage(expectedImage, 0, 0);
					context.drawImage(actualImage, 0, 0);
					this._initImageMagnifier(native_width, native_height);
				} else {
					// IE workaround
					var actualBlack = new Image();
					actualBlack.onload = function() {
						context.drawImage(expectedImage, 0, 0);
						context.globalCompositeOperation = 'source-atop';
						context.drawImage(actualBlack, 0, 0);
						context.globalCompositeOperation = 'destination-over';
						context.drawImage(actualImage, 0, 0);
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