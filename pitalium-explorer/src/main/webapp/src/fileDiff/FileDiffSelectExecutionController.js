/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	/**
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name FileDiffSelectExecutionController
	 */
	var fileDiffSelectExecutionController = {
		'__name': 'hifive.pitalium.explorer.controller.FileDiffSelectExecutionController',

		'_testResultDiffLogic': hifive.pitalium.explorer.logic.TestResultDiffLogic,

		'_popup': null,
		'_executionList': null,
		'_selectedIndex': null,
		'_defaultExecution': null,

		'COMPARE_PROPERTIES': ['browserName', 'browserVersion', 'deviceName', 'executionTime',
				'platform', 'platformVersion'],

		'__init': function(context) {
			this._popup = context.args.popup;
		},

		'__ready': function() {
			this._testResultDiffLogic.listTestExecutionsWithEnvironment().done(
					this.own(function(response) {
						this._executionList = response.content;
						if (this._defaultExecution != null) {
							var selected = false;
							for (var i = 0; i < this._executionList.length; i++) {
								if (selected) {
									this._executionList[i].selected = '';
									continue;
								}

								if (hifive.pitalium.explorer.utils.propertyEquals(
										this._defaultExecution, this._executionList[i],
										this.COMPARE_PROPERTIES)) {
									this._selectedIndex = i;
									this._executionList[i].selected = 'selected';
									selected = true;
								} else {
									this._executionList[i].selected = '';
								}
							}
						}
						this.view.update('#executionList', 'screenshotListTemplate', {
							'executions': this._executionList
						});
					}));

			if (this._defaultExecution != null) {
				this.$find('.btn-positive').removeClass('disabled');
			}
		},

		'setDefaultExecution': function(execution) {
			this._defaultExecution = execution;
		},

		'.explorer-test-result click': function(context, $el) {
			this._selectedIndex = $el.data('explorerIndex');
			this.$find('.btn-positive').removeClass('disabled');

			this.$find('.explorer-test-result').removeClass('selected');
			$el.addClass('selected');
		},

		'.btn-negative click': function() {
			this._close(null);
		},

		'.btn-positive click': function(context, $el) {
			if ($el.hasClass('disabled')) {
				return;
			}

			var execution = this._executionList[this._selectedIndex];
			if (this._defaultExecution != null
					&& hifive.pitalium.explorer.utils.propertyEquals(this._defaultExecution,
							execution, this.COMPARE_PROPERTIES)) {
				this._close(null);
				return;
			}

			this._close(execution);
		},

		'_close': function(execution) {
			this._popup.close({
				'execution': execution
			});
		}

	};

	h5.core.expose(fileDiffSelectExecutionController);
})(jQuery);
