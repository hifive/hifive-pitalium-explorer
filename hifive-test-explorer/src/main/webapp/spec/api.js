(function() {
	var apiController = {
		__name: 'com.htmlhifive.testresultexplorer.apiController',

		'button click': function(context, $el) {
			var apiName = $el.data('apiName');
			switch (apiName) {

			// -------------------API use examples -------------------

			case 'timeList':
				// Get test execution time and result
				h5.ajax({
					type: 'get',
					url: hifive.test.explorer.utils.formatUrl('api/listTestExectionTime')
				}).done(this.own(function(timeList) {
					// Write the processing after the data is loaded.
					this._showJson(timeList, $el);
				}));
				break;

			case 'searchTimeList':
				// Get test execution time and result by search criteria
				h5.ajax({
					type: 'get',
					url: hifive.test.explorer.utils.formatUrl('api/listTestExectionTime/search', {
						criteria: "search"
					})
				}).done(this.own(function(searchTimeList) {
					// Write the processing after the data is loaded.
					this._showJson(searchTimeList, $el);
				}));
				break;

			case 'result':
				// Gets list of the test execution result
				h5.ajax({
					type: 'get',
					url: hifive.test.explorer.utils.formatUrl('api/listTestResult', {
						executionTime: "2015_03_05_22_18_29"
					})
				}).done(this.own(function(result) {
					// Write the processing after the data is loaded.
					this._showJson(result, $el);
				}));
				break;

			case 'detail':
				// Gets the test execution result
				h5.ajax({
					type: 'get',
					url: hifive.test.explorer.utils.formatUrl('api/getDetail', {
						id: 57
					})
				}).done(this.own(function(detail) {
					// Write the processing after the data is loaded.
					this._showJson(detail, $el);
				}));
				break;

			case 'expectedId':
				// Gets the right image id
				h5.ajax({
					type: 'get',
					url: hifive.test.explorer.utils.formatUrl('api/getExpectedId', {
						id: 57
					})
				}).done(this.own(function(expectedId) {
					// Write the processing after the data is loaded.
					this._showJson(expectedId, $el);
				}));
				break;

			case 'image':
				// Get image
				this._showImage(apiName, hifive.test.explorer.utils.formatUrl('../image/get', {
					id: 57
				}));
				break;

			case 'diffImage':
				// Get image
				this._showImage(apiName, hifive.test.explorer.utils.formatUrl('../image/getDiff', {
					sourceId: 57,
					targetId: 30
				}));
				break;
			}

			// ------------------- API use examples -------------------
		},

		_showJson: function(obj, $el) {
			var height = $el.closest('tr').height();
			var $pre = $el.siblings('pre');
			$pre.css('height', height - $el.height() - 35);
			$pre.text(JSON.stringify(obj, null, "    "));
		},

		_showImage: function(apiName, url) {
			this.$find('img[data-api-target="' + apiName + '"]').width(200).attr('src', url);
		}
	};
	h5.core.expose(apiController);
})();