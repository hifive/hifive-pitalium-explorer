/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a &qout;logic&quot; for the test result comparison page.
	 * 
	 * @class
	 * @memberOf hifive.test.explorer.logic
	 * @name TestResultDiffLogic
	 */
	var testResultDiffLogic = {
		/**
		 * @memberOf hifive.test.explorer.logic.TestResultDiffLogic
		 */
		__name: 'hifive.test.explorer.logic.TestResultDiffLogic',

		/**
		 * Get details of the screenshot.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultDiffLogic
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
		}
	};

	h5.core.expose(testResultDiffLogic);
})(jQuery);
(function($) {
	/**
	 * This class is a controller for the test result comparison page.
	 * 
	 * @class
	 * @memberOf hifive.test.explorer.controller
	 * @name TestResultDiffController
	 */
	var testResultDiffController = {
		/**
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		__name: 'hifive.test.explorer.controller.TestResultDiffController',

		/**
		 * The &quot;Logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		_testResultDiffLogic: hifive.test.explorer.logic.TestResultDiffLogic,

		_screenshot: {},

		/**
		 * Called after the controller has been initialized.<br>
		 * Get the id of the right screenshot, and update views.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		__ready: function() {
			// Get the id of the test result from url query parameters.
			var queryParams = hifive.test.explorer.utils.getParameters();
			if (!queryParams.hasOwnProperty('id')) {
				alert('ID not found');
				return;
			}

			var id = queryParams.id;

			// Get screenshot details
			this._testResultDiffLogic.getScreenshot(id).done(this.own(function(screenshot) {
				this._screenshot = screenshot;
				this._initializeImageSelector(screenshot.targets);
				this.view.update('#detail', 'testResultListTemplate', {
					testResult: screenshot
				});
			}));
		},

		/**
		 * Initialize the drop down of image selection.
		 * 
		 * @memberOf hifiveTestExplorer.controller.TestResultDiffController
		 * @param {Array} includes the selectors for the test area inclusion.
		 */
		_initializeImageSelector: function(targets) {
			// Generate select options
			var imageSelector = this.$find('#imageSelector');
			if (targets != null && targets.length > 0) {
				var html = '';
				for ( var key in targets) {
					var target = targets[key];
					html += '<option value="' + target.targetId + '">' + target.area.selectorType
							+ '_' + target.area.selectorValue + '_[' + target.area.selectorIndex
							+ ']</option>';
				}

				imageSelector.prop('disabled', false);
				imageSelector.append(html);
			}

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

			var expectedScreenshot = this._screenshot.expectedScreenshot;
			// Expected mode
			if (expectedScreenshot == null) {
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
					screenshotId: expectedScreenshot.id,
					targetId: targetId
				});
			} else {
				// Test failed
				this._setActualImageSrc(true, {
					sourceSceenshotId: screenshotId,
					targetScreenshotId: expectedScreenshot.id,
					targetId: targetId
				});

				this._setExpectedImageSrc(true, {
					sourceSceenshotId: expectedScreenshot.id,
					targetScreenshotId: screenshotId,
					targetId: targetId
				});
			}

			this._initEdgeOverlapping(expectedScreenshot.id, screenshotId, targetId);

			this._initializeSwipeHandle();
			this._initializeOnionHandle();

			this.$find('#quick-flipping .image-diff.actual').css('opacity', 1);
			this.$find('#quick-flipping .image-diff.expected').css('opacity', 0.2);
			this.$find('#quick-flipping .image-overlay .expected').hide();
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
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 * @param {Boolean} withMarker whether or not to display the image with markers.
		 * @param {Object} params extra paramters
		 */
		_setActualImageSrc: function(withMarker, params) {
			this._setImageSrc('.actual img', withMarker, params);
		},

		/**
		 * Show expected image.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 * @param {Boolean} withMarker whether or not to display the image with markers.
		 * @param {Object} params extra paramters
		 */
		_setExpectedImageSrc: function(withMarker, params) {
			this._setImageSrc('.expected img', withMarker, params);
		},


		/**
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 * @param {Number} expectedId ID of expected image
		 * @param {Number} actualId ID of actual image
		 * @param {Number} targetId ID the target area to be used for image comparison
		 */
		_initEdgeOverlapping: function(expectedId, actualId, targetId) {
			// Initialize <canvas>
			var expected = new Image(),actual = new Image();

			var d1 = $.Deferred(),d2 = $.Deferred();
			expected.onload = d1.resolve;
			actual.onload = d2.resolve;

			var format = hifive.test.explorer.utils.formatUrl;
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

			$.when.apply($, [d1.promise(), d2.promise()]).done(
					function() {
						var canvas = $('#edge-overlapping canvas')[0];
						var native_width = canvas.width = expected.width;
						var native_height = canvas.height = expected.height;

						var context = canvas.getContext('2d');
						context.globalCompositeOperation = 'multiply';
						if (context.globalCompositeOperation == 'multiply') {
							context.drawImage(expected, 0, 0);
							context.drawImage(actual, 0, 0);
							initImageMagnifier();
						} else {
							// IE workaround
							var actualBlack = new Image();
							actualBlack.onload = function() {
								context.drawImage(expected, 0, 0);
								context.globalCompositeOperation = 'source-atop';
								context.drawImage(actualBlack, 0, 0);
								context.globalCompositeOperation = 'destination-over';
								context.drawImage(actual, 0, 0);
								initImageMagnifier();
							};
							actualBlack.src = format('image/getProcessed', {
								id: actualId,
								algorithm: 'edge',
								colorIndex: 2
							});
						}

						function initImageMagnifier() {
							// Image magnifier
							$('.large').css('background-image',
									'url(' + canvas.toDataURL('image/png') + ')');
							$('#edge-overlapping .image-overlay').mousemove(
									function(e) {
										var magnify_offset = $(this).offset();
										var mx = e.pageX - magnify_offset.left;
										var my = e.pageY - magnify_offset.top;

										if (mx < $(this).width() && my < $(this).height() && mx > 0
												&& my > 0) {
											$('.large').fadeIn(100);
										} else {
											$('.large').fadeOut(100);
										}
										if ($('.large').is(':visible')) {
											var rx = Math.round(mx / $('.small').width()
													* native_width - $('.large').width() / 2)
													* -1;
											var ry = Math.round(my / $('.small').height()
													* native_height - $('.large').height() / 2)
													* -1;

											$('.large').css({
												left: mx - $('.large').width() / 2,
												top: my - $('.large').height() / 2,
												backgroundPosition: rx + 'px ' + ry + 'px'
											});
										}
									});
						}
					});
		},

		/**
		 * Show image.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 * @param {String} selector jQuery selector expression which determines the image node
		 * @param {Boolean} withMarker whether or not to display the image with markers.
		 * @param {Object} params extra paramters
		 */
		_setImageSrc: function(selector, withMarker, params) {
			var url = withMarker ? 'image/getDiff' : 'image/get';
			this.$find(selector).attr('src', hifive.test.explorer.utils.formatUrl(url, params));
		},

		/**
		 * Hide actual mode.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		_hideActualMode: function() {
			this.$find('#actual-mode').hide();
		},

		/**
		 * Hide expected mode.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		_hideExpectedMode: function() {
			this.$find('#expected-mode').hide();
		},

		/**
		 * Initialize the swipe diff handle.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
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
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
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
	};

	h5.core.expose(testResultDiffController);
})(jQuery);
$(function() {
	h5.core.controller('body>div.container',
			hifive.test.explorer.controller.TestResultDiffController);
});
