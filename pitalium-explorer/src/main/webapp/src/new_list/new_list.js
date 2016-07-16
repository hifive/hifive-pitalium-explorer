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
		