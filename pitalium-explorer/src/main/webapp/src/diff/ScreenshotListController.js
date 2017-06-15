/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
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
			if (!screenshot) {
				return;
			}

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
																	expScreenshotMap, screenshot);
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
									this
											.own(function(expScreenshotMap) {
												this._showList(screenshotMap, expScreenshotMap,
														screenshot);
											}));

						}));
			}

			return this._testResultDiffLogic.listScreenshot(screenshot.testExecution.id,
					screenshot.testEnvironment.id).done(this.own(function(screenshotMap) {
				this._showList(screenshotMap, null, screenshot);
			}));

		},

		_showList: function(screenshotMap, expectedScreenshotMap, screenshot) {
			this._merge(screenshotMap, expectedScreenshotMap);

			var treeData = [];
			var selectedScreenshotId = screenshot.id;
			var selectedExpectedScreenshotId = screenshot.expectedScreenshotId;
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
						if (selectedScreenshotId == null) {
							selectedScreenshotId = s.id;
							selectedExpectedScreenshotId = s.expectedScreenshotId;
						}
						if (s.id === selectedScreenshotId
								&& s.expectedScreenshotId === selectedExpectedScreenshotId) {
							selected = true;
						}

						child.children.push({
							text: s.screenshotName,
							icon: false,
							a_attr: {
								'class': 'screenshot',
								'data-explorer-screenshot-id': s.id,
								'data-explorer-expected-screenshot-id': s.expectedScreenshotId
							},
							state: {
								opened: true,
								selected: selected
							}
						});

						var iconText = '';
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

			var isInit = false;
			if (!this._$tree) {
				this._$tree = this.$find('#tree_root');
				isInit = true;
			} else {
				this._$tree.jstree(true).destroy();
			}

			this._$tree.jstree({
				'core': {
					data: treeData
				}
			});

			if (!isInit) {
				this.trigger('selectScreenshot', {
					id: selectedScreenshotId,
					expectedId: selectedExpectedScreenshotId
				});
			}
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
		}
	};

	h5.core.expose(screenshotListController);

})(jQuery);