(function() {
	'use strict';

	var DATE_FORMAT_OPTIONS = {
		weekday: "long", year: "numeric", month: "short",
		day: "numeric", hour: "2-digit", minute: "2-digit"
	};

	/**
	 * @param {Date} date
	 * @returns {String}
	 */
	function toLocaleTimeString(date) {
		return date.toLocaleTimeString('en', DATE_FORMAT_OPTIONS);
	}

	/**
	 * @class hifive.pitalium.explorer.newList.PageController
	 */
	/**
	 * @lends hifive.pitalium.explorer.newList.PageController#
	 */
	var PageController = {
		/**
		 * @ignore
		 */
		__name: 'hifive.pitalium.explorer.newList.PageController',
		/**
		 * @ignore
		 */
		__templates: 'src/new_list/new_list.ejs',

		/**
		 * @type {hifive.pitalium.explorer.newList.PageLogic}
		 */
		_pageLogic: hifive.pitalium.explorer.newList.PageLogic,

		/**
		 * @type {JQuery}
		 */
		_$root: null,
		/**
		 * @type {JQuery}
		 */
		_$navBar: null,
		/**
		 * @type {JQuery}
		 */
		_$backgroundPattern: null,


		__ready: function() {
			this._$root = $(this.rootElement);
			this._$navBar = this.$find('.navbar');
			this._$backgroundPattern = this.$find('.background-pattern');

			this._loadList();
		},

		_loadList: function() {
			this._pageLogic.fetchList().done(this.own(function(data) {
				var contents = data.content;

				for (var i = 0; i < contents.length; i++) {
					var content = contents[i];
					var name = content.name;
					var tString = content.timestamp + '';

					var tStamp = new Date(parseInt(tString));
					var mytimes = toLocaleTimeString(tStamp);
					var dir_timestamp = String(content.dirTimestamp);
					var path = dir_timestamp + "/" + name;

					var NofScreenshots = content.numberOfScreenshots;
					var NofResult = content.numberOfResults;

					this.view.append("#result_list", "mylist", {
						id: content.id,
						name: name,
						time: mytimes,
						screenshot: NofScreenshots,
						result: NofResult,
						path: path
					});
				}
			}));
		},

		'{window} scroll': function() {
			var height = this._$root.scrollTop();
			if (height > 80) {
				this._$navBar.fadeIn();
			} else {
				this._$navBar.fadeOut();
			}

			if (height < 86) {
				this._$backgroundPattern.fadeIn();
			} else {
				this._$backgroundPattern.fadeOut();
			}
		},

		'.appendTable click': function(context, $el) {
			var id = $el.data("id");
			var path = $el.data("path");

			this.$find("#table_list_" + id).slideToggle();

			this._appendScreenshots(path, id);
		},

		_appendScreenshots: function(path, id) {
			var table = this.$find("#table_" + id);
			var table_list = this.$find("#table_list_" + id);

			if (table_list.css("display") != "none") {

				//to get screenshots list from API
				this._pageLogic.fetchScreenshotList(path).done(this.own(function(data) {
					table.find(".tr").remove();
					this.$find("#result_ul_" + id).html('');

					var feedback = data.screenshotFileList;
					var resultList = data.resultList;


					for (var i = 0; i < resultList.length; i++) {
						var result = resultList[i];
						var tStamp = new Date(parseInt(result.executionTime));

						this.$find("#result_h_" + id).show();
						this.view.append('#result_ul_' + id, "result_info_table", {
							id: result.id,
							resultList: result.resultList,
							expected: result.expectedFilename,
							timestamp: toLocaleTimeString(tStamp),
							directory: path
						});
					}

					for (var i = 0; i < feedback.length; i++) {
						var tc = feedback[i];
						this.view.append(table, "screenshot_list", {
							id: id,
							name: tc.name,
							timestamp: tc.timestamp,
							platform: tc.platform,
							browser: tc.browser,
							version: tc.version,
							width: tc.width,
							height: tc.height,
							directory: path,
							results: resultList
						});
					}
				}));
			}
		},

		'.expected click': function(context, $el) {
			var directory = $el.data("directory");
			var target = $el.val();

			this.$find(".btn-run").hide();
			this.$find(".expected").show();

			this._findInput(2, directory, target).show(); // To show 'Run-button' in expected element's row

			this.$find(".compare").show();
			var this_checkbox = this._findInput(1, directory, target);  //To find and hide [expected element's campare checkbox]
			var all_checkbox = this._findInput(1, directory); //To find [compare elements checkboxes]

			all_checkbox.show();//To show [compare elements checkboxes]

			all_checkbox.prop("checked", true);

			this_checkbox.hide();  //To hide [expected element's campare checkbox]
			this_checkbox.removeAttr("checked");
		},

		'.btn-run click': function(context, $el) {
			$el.hide();

			var loading = $el.parent().find("img");
			loading.show(); //loading-circle gif image show

			var targets = this.$find(".compare:checked").map(function() {
				var $this = $(this);
				return $this.data("directory") + "/" + $this.data("target");
			}).get();

			var directory_expected = $el.data("directory");
			var expected = $el.data("target");
			this._pageLogic.compareScreenshots(targets.join(','), directory_expected + "/" + expected).done(this.own(function() {
				this._appendScreenshots(directory_expected, $el.data("id"));
				loading.hide();
				$el.show();
			}));
		},

		'.result_info click': function(context, $el) {
			var info_table = $el.parent().find(".info_table_div");
			info_table.slideToggle();

			var result_icon = $el.parent().find(".result_icon");

			if (info_table.data("hided") == "true") {
				info_table.data("hided", "false");
				result_icon.addClass("glyphicon-menu-right");
				result_icon.removeClass("glyphicon-menu-down");
			} else {
				info_table.data("hided", "true");
				result_icon.addClass("glyphicon-menu-down");
				result_icon.removeClass("glyphicon-menu-right");
			}
		},

		'.delete-btn click': function(context, $el) {
			var directory = $el.data("directory");
			var id = $el.data("resultid");

			this.$find("#result_info_" + id).remove();
			alert("Results deleted!");

			this._pageLogic.deleteScreenshot(directory, id);
		},

		_findInput: function(type, directory, target) {

			//if type == 0, this function return the radio buttons in the expected-row
			//if type == 1, this function return the checkboxes in the compare-row
			//if type == 2, this function return the [Run] button in the selected column

			if (target == null) {
				switch (parseInt(type)) {
					case 0:
						return this._$root.find("input[type=radio][data-directory='" + directory + "']");
					case 1:
						return this._$root.find("input[type=checkbox][data-directory='" + directory + "']");
					case 2:
						return this._$root.find("a[data-directory='" + directory + "']");
				}
			}

			switch (parseInt(type)) {
				case 0:
					return this._$root.find("input[type=radio][data-directory='" + directory + "'][data-target='" + target + "']");
				case 1:
					return this._$root.find("input[type=checkbox][data-directory='" + directory + "'][data-target='" + target + "']");
				case 2:
					return this._$root.find("a[data-directory='" + directory + "'][data-target='" + target + "']");
			}
		}
	};

	h5.core.expose(PageController);


})();