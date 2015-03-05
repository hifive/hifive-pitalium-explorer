/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function() {
	/**
	 * @namespace hifive.test.explorer.utils
	 */

	h5.u.obj.expose('hifive.test.explorer.utils', {
		/**
		 * Get page query parameters as a key-value object.
		 * 
		 * @returns {Object} key-value pair object which contains page query parameters
		 * @memberOf hifive.test.explorer.utils
		 */
		getParameters: function() {
			var ret = {};
			var params = location.href.match(/\?(.+)/);

			if (params != null && params.length > 0) {
				params = params[1].split('&');

				for (var i = 0, len = params.length; i < len; i++) {
					var pair = params[i].split('=');

					if (pair.legth === 0) {
						continue;
					}

					var dencodeParam = pair[1];
					ret[pair[0]] = decodeURIComponent(dencodeParam.replace(/\+/g, '%20'));
				}
			}

			return ret;
		},

		/**
		 * Concatenates the base URL and query parameters and generate a new URL.
		 * 
		 * @function
		 * @name formatUrl
		 * @memberOf hifive.test.explorer.utils
		 * @param {String} baseUrl base URL
		 * @param {Object} params query parameters
		 * @returns {String} new concatenated URL
		 */
		formatUrl: function(baseUrl, params) {
			var url = baseUrl;
			if (baseUrl.split('/')[1] === 'resources') {
				if (params) {
					url += '/' + params.id;
				}
				return url;
			}


			var paramStr = '';
			if (params) {
				for ( var key in params) {
					paramStr += '&' + key + '=' + encodeURI(params[key]);
				}
			}
			return url + paramStr.replace('&', '?');
		}
	});
})();