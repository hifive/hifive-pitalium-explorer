(function() {
	'use strict';

	/**
	 * @class hifive.pitalium.explorer.browserDiff.PageLogic
	 */
	/**
	 * @lends hifive.pitalium.explorer.browserDiff.PageLogic#
	 */
	var PageLogic = {
		/**
		 * @ignore
		 */
		__name: 'hifive.pitalium.explorer.browserDiff.PageLogic',

		/**
		 * @param {String} target
		 * @param {String} expected
		 * @returns {JQueryPromise}
		 */
		fetchScreenshotImages: function(target, expected) {
			return h5.ajax({
				type: 'GET',
				url: '_screenshots/images.json',
				data: {
					target: target,
					expected: expected
				}
			});
		},

		/**
		 * @param {String} path
		 * @param {String} resultListId
		 * @param {String} targetResultId
		 * @returns {JQueryPromise}
		 */
		fetchCompareResults: function(path, resultListId, targetResultId) {
			return h5.ajax({
				url: '_screenshots/result.json',
				data: {
					path: path,
					resultListId: resultListId,
					targetResultId: targetResultId
				}
			});
		}


	};

	h5.core.expose(PageLogic);

})();