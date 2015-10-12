/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
$(function() {
	h5.core.controller('body>div.container',
			hifive.pitalium.explorer.controller.TestResultListController);
});
