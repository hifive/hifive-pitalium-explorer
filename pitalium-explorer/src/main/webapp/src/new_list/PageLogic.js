(function() {
	'use strict';

	/**
	 * @class hifive.pitalium.explorer.newList.PageLogic
	 */
	/**
	 * @lends hifive.pitalium.explorer.newList.PageLogic#
	 */
	var PageLogic = {
		/**
		 * @ignore
		 */
		__name: 'hifive.pitalium.explorer.newList.PageLogic',

		/**
		 * @returns {JQueryPromise}
		 */
		fetchList: function() {
			return h5.ajax('./_results/list.json');
		},

		/**
		 * @param {String} path
		 * @param {Boolean} [refresh = true]
		 */
		fetchScreenshotList: function(path, refresh) {
			if (refresh !== false) {
				refresh = true;
			}

			return h5.ajax({
				url: './_screenshots/list.json',
				data: {
					path: path,
					refresh: refresh + ''
				}
			});
		},

		/**
		 *
		 * @param {String} targets
		 * @param {String} expected
		 * @returns {JQueryPromise}
		 */
		compareScreenshots: function(targets, expected) {
			return h5.ajax({
				type: 'POST',
				url: '_screenshots/compare.json',
				data: {
					targets: targets,
					expected: expected
				}
			});
		},

		/**
		 * @param {String} path
		 * @param {String} id
		 * @returns {JQueryPromise}
		 */
		deleteScreenshot: function(path, id) {
			return h5.ajax({
				url: '_screenshots/delete',
				data: {
					path: path,
					resultListId: id
				}
			});
		}

	};

	h5.core.expose(PageLogic);

})();