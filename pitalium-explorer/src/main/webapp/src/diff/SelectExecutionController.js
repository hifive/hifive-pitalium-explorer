/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
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

		_executionList: null,

		__init: function(context) {
			this._popup = context.args.popup;
		},

		__ready: function(context) {
			this._testResultDiffLogic.listTestExecutionsWithEnvironment().done(
					this.own(function(response) {
						this._executionList = response.content;
						this.view.update('#execution_list', 'screenshotListTemplate', {
							executions: this._executionList
						});

						var that = this;
						var $results = $('#detail .explorer-test-result');
						$results.each(function(index, elem) {
							var $tds = $(elem).find('td');
							var screenshot = {
								executionTime: $tds[0].textContent,
								platform: $tds[4].textContent,
								browserName: $tds[5].textContent,
								browserVersion: $tds[6].textContent
							};
							if (screenshot.browserVersion === '') {
								screenshot.browserVersion = null;
							}

							for (var i = 0, len = that._executionList.length; i < len; i++) {
								var e = that._executionList[i];
								if (screenshot.executionTime === e.executionTime
										&& screenshot.platform === e.platform
										&& screenshot.browserName === e.browserName
										&& screenshot.browserVersion === e.browserVersion) {
									var selector;
									if (index === 0) {
										selector = '.actual:eq(' + i + ')';
									} else if (index === 1) {
										selector = '.expected:eq(' + i + ')';
									}
									that.$find(selector).click();
									break;
								}
							}
						});
					}));
		},

		'.actual click': function(context, $el) {
			if ($el.hasClass('btn-danger')) {
				return;
			}

			var $actualButton = this.$find('button.actual.btn-danger');
			if ($actualButton.length !== 0) {
				$actualButton.removeClass('btn-danger').addClass('btn-default');
			}

			var $expectedButton = $el.next();
			var $expectedExecution = this.$find('#expectedExecution');
			if ($expectedButton.hasClass('btn-info')) {
				$expectedButton.removeClass('btn-info').addClass('btn-default');

				$expectedExecution.removeAttr('data-expected-explorer-index');
				$expectedExecution.removeData('expectedExplorerIndex');
				$expectedExecution.find('.executionTime').empty();
				$expectedExecution.find('.platform').empty();
				$expectedExecution.find('.browserName').empty();
				$expectedExecution.find('.browserVersion').empty();
			}

			$el.addClass('btn-danger');

			var index = $el.parent().parent().data('explorerIndex');
			var e = this._executionList[index];

			var $actualExecution = this.$find('#actualExecution');
			$actualExecution.data('actualExplorerIndex', index);
			$actualExecution.attr('data-actual-explorer-index', index);
			$actualExecution.find('.executionTime').text(e.executionTime);
			$actualExecution.find('.platform').text(e.platform);
			$actualExecution.find('.browserName').text(e.browserName);
			$actualExecution.find('.browserVersion').text(e.browserVersion);

			if ($expectedExecution.data('expectedExplorerIndex') != null) {
				this.$find('.ok').prop('disabled', false);
			} else {
				this.$find('.ok').prop('disabled', true);
			}
		},

		'.expected click': function(context, $el) {
			if ($el.hasClass('btn-info')) {
				return;
			}

			var $expectedButton = this.$find('button.expected.btn-info');
			if ($expectedButton.length !== 0) {
				$expectedButton.removeClass('btn-info').addClass('btn-default');
			}

			var $actualButton = $el.prev();
			var $actualExecution = this.$find('#actualExecution');
			if ($actualButton.hasClass('btn-danger')) {
				$actualButton.removeClass('btn-danger').addClass('btn-default');

				$actualExecution.removeAttr('data-actual-explorer-index');
				$actualExecution.removeData('actualExplorerIndex');
				$actualExecution.find('.executionTime').empty();
				$actualExecution.find('.platform').empty();
				$actualExecution.find('.browserName').empty();
				$actualExecution.find('.browserVersion').empty();
			}

			$el.addClass('btn-info');

			var index = $el.parent().parent().data('explorerIndex');
			var e = this._executionList[index];

			var $expectedExecution = this.$find('#expectedExecution');
			$expectedExecution.data('expectedExplorerIndex', index);
			$expectedExecution.attr('data-expected-explorer-index', index);
			$expectedExecution.find('.executionTime').text(e.executionTime);
			$expectedExecution.find('.platform').text(e.platform);
			$expectedExecution.find('.browserName').text(e.browserName);
			$expectedExecution.find('.browserVersion').text(e.browserVersion);

			if ($actualExecution.data('actualExplorerIndex') != null) {
				this.$find('.ok').prop('disabled', false);
			} else {
				this.$find('.ok').prop('disabled', true);
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