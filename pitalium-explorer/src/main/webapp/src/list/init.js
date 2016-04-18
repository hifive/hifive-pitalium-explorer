/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
$(function() {
	h5.core.controller('body>div.container',
			hifive.pitalium.explorer.controller.ResultListPageController);
});


$(window).scroll(function () {
	var height = $(document).scrollTop();
	if (height > 80) {
		
		$(".navbar").fadeIn();
		
		
	} else{
		
		$(".navbar").fadeOut();
		
	}
	
	if(height < 86){
		
		$(".background-pattern").fadeIn();
		
	}else{
		$(".background-pattern").fadeOut();
	}
	
	
	
});

