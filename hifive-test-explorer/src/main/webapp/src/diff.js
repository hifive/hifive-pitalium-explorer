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
		 * Get details of the test result.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the test result
		 * @returns {JqXHRWrapper}
		 */
		getDetail: function(id) {
			return h5.ajax({
				type: 'get',
				url: 'api/getDetail',
				data: {
					id: id
				}
			});
		},

		/**
		 * Get the ID of the right screenshot of the test result.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the test result
		 * @returns {JqXHRWrapper}
		 */
		getExpectedId: function(id) {
			return h5.ajax({
				type: 'get',
				url: 'api/getExpectedId',
				data: {
					id: id
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

			// Get test result details
			this._testResultDiffLogic.getDetail(id).done(this.own(function(detail) {
				// Expected mode
				if (detail.mode == 'EXPECTED') {
					this._setActualImageSrc(false, {
						id: id
					});
					return;
				}

				this._testResultDiffLogic.getExpectedId(id).done(this.own(function(result) {
					// Test not executed
					if (detail.comparisonResult == null) {
						this._setExpectedImageSrc(false, {
							id: result.id
						});
						return;
					}

					if (detail.comparisonResult) {
						// Test succeeded
						this._setActualImageSrc(false, {
							id: id
						});

						this._setExpectedImageSrc(false, {
							id: result.id
						});
					} else {
						// Test failed
						this._setActualImageSrc(true, {
							sourceId: id,
							targetId: result.id
						});

						this._setExpectedImageSrc(true, {
							sourceId: result.id,
							targetId: id
						});
					}
				}));
			}));
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
		}
	};

	h5.core.expose(testResultDiffController);
})(jQuery);
$(function() {
	h5.core.controller('body>div.container',
			hifive.test.explorer.controller.TestResultDiffController);
});