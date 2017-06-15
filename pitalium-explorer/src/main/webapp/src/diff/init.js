/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
$(function() {
	h5.core.controller('#container', hifive.pitalium.explorer.controller.DiffPageController);
});