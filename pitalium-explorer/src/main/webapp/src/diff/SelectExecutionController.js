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
					}));
		},

		'.actual click': function(context, $el) {
			if ($el.hasClass('active')) {
				return;
			}

			this.$find('button.actual.active').removeClass('active');
			$el.addClass('active');

			this.$find('button.expected[disabled]').prop('disabled', false);
			var $expectedButton = $el.next();
			$expectedButton.prop('disabled', true);

			var index = $el.parent().parent().data('explorerIndex');
			var e = this._executionList[index];

			this.$find('#actualExecution').attr('data-actual-explorer-index', index);
			this.$find('#actualExecution .executionTime').text(e.executionTime);
			this.$find('#actualExecution .platform').text(e.platform);
			this.$find('#actualExecution .browserName').text(e.browserName);
			this.$find('#actualExecution .browserVersion').text(e.browserVersion);

			if (this.$find('#expectedExecution').data('expectedExplorerIndex') != null) {
				this.$find('.ok').prop('disabled', false);
			}
		},

		'.expected click': function(context, $el) {
			if ($el.hasClass('active')) {
				return;
			}

			this.$find('button.expected.active').removeClass('active');
			$el.addClass('active');

			this.$find('button.actual[disabled]').prop('disabled', false);
			var $actualButton = $el.prev();
			$actualButton.prop('disabled', true);

			var index = $el.parent().parent().data('explorerIndex');
			var e = this._executionList[index];

			this.$find('#expectedExecution').attr('data-expected-explorer-index', index);
			this.$find('#expectedExecution .executionTime').text(e.executionTime);
			this.$find('#expectedExecution .platform').text(e.platform);
			this.$find('#expectedExecution .browserName').text(e.browserName);
			this.$find('#expectedExecution .browserVersion').text(e.browserVersion);

			if (this.$find('#actualExecution').data('actualExplorerIndex') != null) {
				this.$find('.ok').prop('disabled', false);
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