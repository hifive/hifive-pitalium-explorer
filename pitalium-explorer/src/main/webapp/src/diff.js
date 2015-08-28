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
				url: 'screenshot',
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

			h5.ajax('screenshot/list', {
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
		},

		listCompositeTestExecution: function() {
			return h5.ajax({
				type: 'get',
				url: 'compositeExecution/list'
			});
		},

		getComparisonResult: function(screenshot, targetId) {
			return h5.ajax('comparisonResult', {
				data: {
					sourceScreenshotId: screenshot.id,
					targetScreenshotId: screenshot.expectedScreenshotId,
					targetId: targetId,
				},
				type: 'GET',
				dataType: 'json'
			});
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
	var selectExecutionController = {
		__name: 'hifive.pitalium.explorer.controller.SelectExecutionController',

		/**
		 * The &quot;Logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.SelectExecutionController
		 */
		_testResultDiffLogic: hifive.pitalium.explorer.logic.TestResultDiffLogic,

		_$selected: null,

		_executionList: null,

		__init: function(context) {
			this._popup = context.args.popup;
		},

		__ready: function(context) {
			this._testResultDiffLogic.listCompositeTestExecution().done(
					this.own(function(response) {
						this._executionList = response.content;
						this.view.update('#execution_list', 'screenshotListTemplate', {
							executions: this._executionList
						});
					}));
		},

		'[name="execution"] change': function(context, $el) {
			if (this._$selected) {
				this._$selected.removeClass('success');
			}
			this._$selected = $el.parent().parent();
			this._$selected.addClass('success');
		},

		'.actual click': function() {
			if (!this._$selected) {
				return;
			}

			var index = this._$selected.data('explorerIndex');
			var e = this._executionList[index];

			this.$find('#actualExecution').attr('data-actual-explorer-index', index);
			this.$find('#actualExecution #executionTime').text(e.executionTime);
			this.$find('#actualExecution #platform').text(e.platform);
			this.$find('#actualExecution #browserName').text(e.browserName);
			this.$find('#actualExecution #browserVersion').text(e.browserVersion);

			if (this.$find('#expectedExecution').data('expectedExplorerIndex') != null) {
				this.$find('.ok').show();
			}
		},

		'.expected click': function() {
			if (!this._$selected) {
				return;
			}

			var index = this._$selected.data('explorerIndex');
			var e = this._executionList[index];

			this.$find('#expectedExecution').attr('data-expected-explorer-index', index);
			this.$find('#expectedExecution #executionTime').text(e.executionTime);
			this.$find('#expectedExecution #platform').text(e.platform);
			this.$find('#expectedExecution #browserName').text(e.browserName);
			this.$find('#expectedExecution #browserVersion').text(e.browserVersion);

			if (this.$find('#actualExecution').data('actualExplorerIndex') != null) {
				this.$find('.ok').show();
			}
		},

		'.ok click': function() {
			var actualIndex = this.$find('#actualExecution').data('actualExplorerIndex');
			var expectedIndex = this.$find('#expectedExecution').data('expectedExplorerIndex');
			if (actualIndex == null || expectedIndex == null) {
				return;
			}

			var actualExecution = this._executionList[actualIndex];
			var expectedExecution = this._executionList[expectedIndex];

			this._popup.close({
				testExecution: {
					id: actualExecution.executionId,
					timeString: actualExecution.executionTime
				},
				testEnvironment: {
					id: actualExecution.environmentId,
					browserName: actualExecution.browserName
				},
				expectedTestExecution: {
					id: expectedExecution.executionId,
					timeString: expectedExecution.executionTime
				},
				expectedTestEnvironment: {
					id: expectedExecution.environmentId,
					browserName: expectedExecution.browserName
				}
			});
		},

		'.cancel click': function() {
			this._popup.close();
		}
	};

	h5.core.expose(selectExecutionController);
})(jQuery);
(function($) {

	var SelectExecutionControllerDef = hifive.pitalium.explorer.controller.SelectExecutionController;

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
			if (!screenshot) {
				return;
			}

			this.$find('#time').text(screenshot.testExecution.timeString);
			this.$find('#browser_name').text(screenshot.testEnvironment.browserName);

			if (screenshot.expectedScreenshotId != null) {
				return this._testResultDiffLogic.listScreenshot(screenshot.testExecution.id,
						screenshot.testEnvironment.id).done(
						this.own(function(screenshotMap) {
							this._testResultDiffLogic
									.getScreenshot(screenshot.expectedScreenshotId).done(
											this.own(function(expScreenshot) {
												this._testResultDiffLogic.listScreenshot(
														expScreenshot.testExecution.id,
														expScreenshot.testEnvironment.id).done(
														this.own(function(expScreenshotMap) {
															this._showList(screenshotMap,
																	expScreenshotMap);
														}));

											}));
						}));
			}

			if (screenshot.expectedTestExecution != null
					&& screenshot.expectedTestEnvironment != null) {
				return this._testResultDiffLogic.listScreenshot(screenshot.testExecution.id,
						screenshot.testEnvironment.id).done(
						this.own(function(screenshotMap) {
							this._testResultDiffLogic.listScreenshot(
									screenshot.expectedTestExecution.id,
									screenshot.expectedTestEnvironment.id).done(
									this.own(function(expScreenshotMap) {
										this._showList(screenshotMap, expScreenshotMap);
									}));

						}));
			}

			return this._testResultDiffLogic.listScreenshot(screenshot.testExecution.id,
					screenshot.testEnvironment.id).done(this.own(function(screenshotMap) {
				this._showList(screenshotMap, null);
			}));

		},

		_showList: function(screenshotMap, expectedScreenshotMap) {
			this._merge(screenshotMap, expectedScreenshotMap);

			var treeData = [];
			var firstScreenshotId = null;
			var firstExpectedScreenshotId = null;
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
						var selected = false;
						if (!firstScreenshotId) {
							firstScreenshotId = s.id;
							firstExpectedScreenshotId = s.expectedScreenshotId;
							selected = true;
						}
						child.children.push({
							text: s.screenshotName,
							icon: false,
							a_attr: {
								'class': 'screenshot',
								'data-explorer-screenshot-id': s.id,
								'data-explorer-expected-screenshot-id': s.expectedScreenshotId,
							},
							state: {
								opened: true,
								selected: selected
							}
						});

						var iconText = null;
						if (s.existsExpected) {
							iconText += "<span class='glyphicon glyphicon-file expected'></span>";
						} else {
							iconText += "<span class='glyphicon glyphicon-file expected none'></span>";
						}
						if (s.existsActual) {
							iconText += "<span class='glyphicon glyphicon-file actual'></span>";
						} else {
							iconText += "<span class='glyphicon glyphicon-file actual none'></span>";
						}
						child.children[i].text += iconText;
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

			if (!this._$tree) {
				this._$tree = this.$find('#tree_root');
				this._$tree.jstree({
					'core': {
						data: treeData
					}
				});
			} else {
				this._$tree.jstree(true).settings.core.data = treeData;
				this._$tree.jstree(true).refresh();
			}

			this.trigger('selectScreenshot', {
				id: firstScreenshotId,
				expectedId: firstExpectedScreenshotId
			});
		},

		_merge: function(screenshotMap, expectedScreenshotMap) {
			for ( var testClass in screenshotMap) {
				var testMethodMap = screenshotMap[testClass];
				for ( var testMethod in testMethodMap) {
					var screenshots = testMethodMap[testMethod];
					for (var i = 0, len = screenshots.length; i < len; i++) {
						var s = screenshots[i];
						// add flag;
						s.existsActual = true;
						s.existsExpected = false;
						s.expectedScreenshotId = null;
					}
				}
			}

			// merge
			if (expectedScreenshotMap != null) {
				for ( var testClass in expectedScreenshotMap) {
					var testMethodMap = screenshotMap[testClass];
					if (testMethodMap) {
						var expectedTestMethodMap = expectedScreenshotMap[testClass];
						for ( var testMethod in expectedTestMethodMap) {
							var screenshots = testMethodMap[testMethod];
							if (screenshots) {
								var expectedScreenshots = expectedTestMethodMap[testMethod];
								for (var j = 0, expectedLen = expectedScreenshots.length; j < expectedLen; j++) {
									var expectedS = expectedScreenshots[j];
									var existsFlag = false;
									for (var i = 0, len = screenshots.length; i < len; i++) {
										var s = screenshots[i];
										if (s.screenshotName == expectedS.screenshotName) {
											existsFlag = true;
											s.existsExpected = true;
											s.expectedScreenshotId = expectedS.id;
											break;
										}
									}

									if (!existsFlag) {
										// screenshots に追加
										screenshots.push(expectedS);
										// add flag;
										expectedS.existsActual = false;
										expectedS.existsExpected = true;
										expectedS.expectedScreenshotId = null;
									}
								}
							} else {
								// testMethodMap に追加
								var expectedScreenshots = expectedTestMethodMap[testMethod];
								testMethodMap[testMethod] = expectedScreenshots;
								for (var j = 0, expectedLen = expectedScreenshots.length; j < expectedLen; j++) {
									var expectedS = expectedScreenshots[j];
									// add flag;
									expectedS.existsActual = false;
									expectedS.existsExpected = true;
									expectedS.expectedScreenshotId = null;
								}
							}
						}
					} else {
						// screenshotMap に追加
						var expectedTestMethodMap = expectedScreenshotMap[testClass];
						screenshotMap[testClass] = expectedTestMethodMap;
						for ( var testMethod in expectedTestMethodMap) {
							var expectedScreenshots = expectedTestMethodMap[testMethod];
							for (var j = 0, expectedLen = expectedScreenshots.length; j < expectedLen; j++) {
								var expectedS = expectedScreenshots[j];
								// add flag;
								expectedS.existsActual = false;
								expectedS.existsExpected = true;
								expectedS.expectedScreenshotId = null;
							}
						}
					}
				}
			}
		},

		'.screenshot click': function(context, $el) {
			var id = $el.data('explorerScreenshotId');
			var expectedId = $el.data('explorerExpectedScreenshotId');
			this.trigger('selectScreenshot', {
				id: id,
				expectedId: expectedId
			});
		},

		'#select_execution click': function() {
			var popup = h5.ui.popupManager.createPopup('execution', 'Select an execution', this
					.$find('#popup_content').html(), SelectExecutionControllerDef, {
				draggable: true
			});
			popup.promise.done(this.own(this.showList));
			popup.setContentsSize(500, 550);
			popup.show();
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
			this.view.update('#detail', 'testResultListTemplate', {
				actual: screenshot,
				expected: expectedScreenshot
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
							this.view.update('#comparisonResult', 'comparisonResultTemplate', {
								comparisonResult: comparisonResult
							});
							this._changeTitle(comparisonResult);
						}));
			} else {
				this._screenshot.comparisonResult = null;
				// Fire change event and show images.
				this._setImage(targetId);
				this.view.update('#comparisonResult', 'comparisonResultTemplate', {
					comparisonResult: ''
				});
				this._changeTitle(null);
			}
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

		/** current result screenshot id */
		_currentScreenshotId: null,
		_currentExpectedScreenshotId: null,

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
			this._currentScreenshotId = id;

			// Get screenshot detailsT
			this._testResultDiffLogic.getScreenshot(id).done(
					this.own(function(screenshot) {
						var expectedScreenshotId = screenshot.expectedScreenshotId;
						this._currentExpectedScreenshotId = expectedScreenshotId;

						var diffPromise = null;
						if (expectedScreenshotId != null) {
							this._testResultDiffLogic.getScreenshot(expectedScreenshotId).done(
									this.own(function(expectedScreenshot) {
										diffPromise = this._testResultDiffController.showResult(
												screenshot, expectedScreenshot);
									}));
						} else {
							diffPromise = this._testResultDiffController.showResult(screenshot,
									null);
						}

						var listPromise = this._screenshotListController.showList(screenshot);

						h5.async.when(diffPromise, listPromise).done(this.own(this._refreshView));
					}));
		},

		_refreshView: function() {
			var height = this.$find('#main')[0].scrollHeight;
			$(this.rootElement).height(height);
			this._dividedboxController.refresh();
		},

		'#list selectScreenshot': function(context, $el) {
			var id = context.evArg.id;
			var expectedId = context.evArg.expectedId;
			if (this._currentScreenshotId == id && this._currentExpectedScreenshotId == expectedId) {
				return;
			}

			this._testResultDiffLogic.getScreenshot(id).done(
					this.own(function(screenshot) {
						// expectedの値を書き換える
						screenshot.expectedScreenshotId = expectedId;
						// 比較結果を書き換える
						if (expectedId == null) {
							this._testResultDiffController.showResult(screenshot, null);
						} else {
							this._testResultDiffLogic.getScreenshot(expectedId).done(
									this.own(function(expectedScreenshot) {
										this._testResultDiffController.showResult(screenshot,
												expectedScreenshot);
									}));
						}
						this._currentScreenshotId = id;
						this._currentExpectedScreenshotId = expectedId;
					}));
		},

		'#main viewChanged': function(context, $el) {
			this._refreshView();
		},

		'{window} [resize]': function() {
			this._refreshView();
		},

		'{rootElement} boxSizeChange': function() {
			var height = this.$find('#main')[0].scrollHeight;
			$(this.rootElement).height(height);
		}
	};

	h5.core.expose(diffPageController);

})(jQuery);
$(function() {
	h5.core.controller('#container', hifive.pitalium.explorer.controller.DiffPageController);
});
