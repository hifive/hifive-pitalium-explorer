/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	var selectController = hifive.pitalium.explorer.controller.FileDiffSelectExecutionController;

	/**
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name ImageListController
	 */
	var imageListController = {
		'__name': 'hifive.pitalium.explorer.controller.ImageListController',

		'_testResultDiffLogic': hifive.pitalium.explorer.logic.TestResultDiffLogic,

		'_mode': null,
		'_$tree': null,
		'_treeData': null,
		'_uploadImageList': null,

		'_enableSelectExecution': true,
		'_selectedExecution': null,

		'__ready': function() {
			this._mode = $(this.rootElement).hasClass('expected') ? 'expected' : 'actual';
			this._$tree = this.$find('.tree-root');

			this._init();
		},

		'_init': function() {
			this._uploadImageList = {
				'text': 'Uploaded images',
				'state': {
					'opened': true
				},
				'children': [],
				'a_attr': {
					'data-screenshot-type': 'directory'
				}
			};
			this._treeData = [this._uploadImageList];

			this._refreshTree();
		},

		'_refreshTree': function() {
			if (this._$tree.hasClass('jstree')) {
				this._$tree.jstree(true).destroy(true);
			}

			var data = this._treeData;
			this._$tree.jstree({
				'core': {
					'data': data
				}
			})
		},

		'mode': function() {
			return this._mode;
		},

		/**
		 * Add temporary uploaded files to file tree.
		 * 
		 * @param {Array} files
		 * @method
		 * @memberOf hifive.pitalium.explorer.controller.ImageListController
		 */
		'addTemporaryFile': function(files) {
			this._resetSelection();

			var first = true;

			var addDirectory = function(file, parent) {
				var dir = {
					'text': file.name,
					'state': {
						'opened': true
					},
					'children': [],
					'a_attr': {
						'data-screenshot-type': 'directory'
					}
				};
				parent.children.push(dir);

				for (var i = 0; i < file.children.length; i++) {
					var f = file.children[i];
					if (f.isFile) {
						addFile(f, dir);
					} else {
						addDirectory(f, dir);
					}
				}
			};

			var addFile = function(file, parent) {
				var _first = first;
				if (first) {
					first = false;
				}

				parent.children.push({
					'text': file.name,
					'icon': false,
					'state': {
						'selected': _first
					},
					'a_attr': {
						'class': 'screenshot',
						'data-screenshot-type': 'temporary',
						'data-screenshot-id': file.screenshotId,
						'data-target-id': 0
					}
				});
			};

			for (var i = 0; i < files.length; i++) {
				var f = files[i];
				if (f.isFile) {
					addFile(f, this._uploadImageList);
				} else {
					addDirectory(f, this._uploadImageList);
				}
			}

			this._refreshTree();
		},

		'_resetSelection': function() {
			var resetSelection = function(obj) {
				// Directory
				if (obj.children) {
					obj.children.forEach(resetSelection);
				}

				obj.state.selected = false;
			};
			this._treeData.forEach(resetSelection);
		},

		'.screenshot click': function(context, $el) {
			var screenshotId = $el.data('screenshotId');
			var targetId = $el.data('targetId');
			var mode = this._mode;
			this.trigger('screenshotSelect', {
				'screenshotId': screenshotId,
				'targetId': targetId,
				'mode': mode
			});
		},

		'.btn-select-execution click': function() {
			if (!this._enableSelectExecution) {
				return;
			}

			var contents = h5.core.view.get('popupContents');
			selectController.setDefaultExecution(this._selectedExecution);
			var popup = h5.ui.popupManager.createPopup('execution', 'Select an execution',
					contents, selectController, {
						'draggable': true
					});

			popup.promise.done(this.own(this._triggerSelectExecution));
			popup.setContentsSize(500, 500);
			popup.show();
		},

		'disableSelectExecution': function() {
			this._enableSelectExecution = false;
			this.$find('.btn-select-execution').addClass('disabled');
		},

		'_triggerSelectExecution': function(screenshot) {
			if (!screenshot.execution) {
				return;
			}

			this._selectedExecution = screenshot.execution;
			this._updateExecutionTree();

			var mode = this._mode;
			this.trigger('selectExecution', {
				'screenshot': screenshot.execution,
				'mode': mode
			});
		},

		'_updateExecutionTree': function() {
			if (this._treeData.length == 1) {
				this._treeData.unshift({
					'text': 'Pitalium execution result',
					'state': {
						'opened': true
					},
					'children': [{
						'text': '',
						'state': {
							'opened': true
						},
						'children': [],
						'a_attr': {
							'data-screenshot-type': 'directory'
						}
					}],
					'a_attr': {
						'data-screenshot-type': 'directory'
					}
				});
			}

			this._treeData[0].children[0].text = this._selectedExecution.executionTime;
			this._treeData[0].children[0].children = [];

			this._refreshTree();

			this._testResultDiffLogic.listScreenshot(this._selectedExecution.executionId,
					this._selectedExecution.environmentId).done(
					this.own(this._updateExecutionTreeChildren));
		},

		'_updateExecutionTreeChildren': function(data) {
			for ( var className in data) {
				var methodTree = [];
				this._treeData[0].children[0].children.push({
					'text': className,
					'state': {
						'opened': false
					},
					'children': methodTree,
					'a_attr': {
						'data-screenshot-type': 'directory'
					}
				});

				var methods = data[className];
				for ( var methodName in methods) {
					var testTree = [];
					methodTree.push({
						'text': methodName,
						'state': {
							'opened': false
						},
						'children': testTree,
						'a_attr': {
							'data-screenshot-type': 'directory'
						}
					});

					var tests = methods[methodName];
					for (var i = 0; i < tests.length; i++) {
						var test = tests[i];
						for (var j = 0; j < test.targets.length; j++) {
							var target = test.targets[j];
							var targetName = this._buildScreenshotTargetName(target);
							testTree.push({
								'text': test.screenshotName + ' ' + targetName,
								'icon': false,
								'state': {},
								'a_attr': {
									'class': 'screenshot',
									'data-screenshot-type': 'execution',
									'data-screenshot-id': test.id,
									'data-target-id': target.targetId
								}
							});
						}
					}
				}
			}

			this._refreshTree();
		},

		'_buildScreenshotTargetName': function(target) {
			var area = target.area;
			var name;
			if (area.selectorType) {
				name = area.selectorType + ': ' + area.selectorValue + ' (' + area.selectorIndex + ')';
			} else {
				name = area.x + ', ' + area.y + ', ' + area.width + ', ' + area.height;
			}

			return '[' + name + ']';
		}
	};

	h5.core.expose(imageListController);
})(jQuery);