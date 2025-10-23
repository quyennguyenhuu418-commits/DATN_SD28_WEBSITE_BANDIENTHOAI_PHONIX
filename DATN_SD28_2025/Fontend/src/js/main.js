window.awe = window.awe || {};
awe.init = function () {
	awe.showPopup();
	awe.hidePopup();	
};
$(document).ready(function ($) {
	"use strict";
	awe_backtotop();
	awe_tab();
	awe_category();
});

$('.dropdown-toggle').click(function() {
	$(this).parent().toggleClass('open'); 	
}); 
$('.close-pop').click(function() {
	$('#popup-cart').removeClass('opencart');
	$('body').removeClass('opacitycart');
});
$(document).on('click','.overlay, .close-popup, .btn-continue, .fancybox-close', function() {   
	hidePopup('.awe-popup'); 	
	setTimeout(function(){
		$('.loading').removeClass('loaded-content');
	},500);
	return false;
})

function awe_showLoading(selector) {
	var loading = $('.loader').html();
	$(selector).addClass("loading").append(loading); 
}  window.awe_showLoading=awe_showLoading;
function awe_hideLoading(selector) {
	$(selector).removeClass("loading"); 
	$(selector + ' .loading-icon').remove();
}  window.awe_hideLoading=awe_hideLoading;
function awe_showPopup(selector) {
	$(selector).addClass('active');
}  window.awe_showPopup=awe_showPopup;
function awe_hidePopup(selector) {
	$(selector).removeClass('active');
}  window.awe_hidePopup=awe_hidePopup;
awe.hidePopup = function (selector) {
	$(selector).removeClass('active');
}
$(document).on('click','.overlay, .close-window, .btn-continue, .fancybox-close', function() {   
	awe.hidePopup('.awe-popup'); 
	setTimeout(function(){
		$('.loading').removeClass('loaded-content');
	},500);
	return false;
})
var wDWs = $(window).width();

if (wDWs < 1199) {
	$('.quickview-product').remove();
}


if (wDWs < 767) {
	$('.footer-click h4').click(function(e){
		$(this).toggleClass('cls_mn').next().slideToggle();
		$(this).next('ul').toggleClass("current");
	});
}
if (wDWs <= 991) {
	$('.menu-bar').on('click', function(){
		$('.opacity_menu').addClass('current');
	})
	$('#nav li > .open_mnu').click(function(e){
		$(this).closest('li').find('> .dropdown-menu').slideToggle("fast");
		$(this).closest('li').toggleClass("current");
		$(this).closest('li').find('> .dropdown-menu').toggleClass("current");
		$(this).toggleClass('current');
		return false;  
	});
	$('.opacity_menu').on('click', function(){
		$('.opacity_menu').removeClass('current');
	})
	$('.header-action-item.search-mobile').click(function(e){
		e.preventDefault();
		$('.search-mobile.search_form').toggleClass('open');
	});
	$('.input-group-btn .search-close').click(function(e){
		e.preventDefault();
		$('.search-mobile.search_form').toggleClass('open');
	});
	$('#btn-menu-mobile').on('click', function(){
		$('.header-menu').addClass('current');
		$('.mobile-nav-overflow').toggleClass('open');

	});
	$('.header-menu .title_menu').on('click', function(){
		$(this).closest('.header-menu').removeClass('current');
		$('.mobile-nav-overflow').toggleClass('open');
	});
	$('.mobile-nav-overflow').on('click', function(){
		$('.header-menu').removeClass('current');
		$(this).toggleClass('open');
	});
}
function awe_convertVietnamese(str) { 
	str= str.toLowerCase();
	str= str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g,"a"); 
	str= str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g,"e"); 
	str= str.replace(/ì|í|ị|ỉ|ĩ/g,"i"); 
	str= str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g,"o"); 
	str= str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g,"u"); 
	str= str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g,"y"); 
	str= str.replace(/đ/g,"d"); 
	str= str.replace(/!|@|%|\^|\*|\(|\)|\+|\=|\<|\>|\?|\/|,|\.|\:|\;|\'| |\"|\&|\#|\[|\]|~|$|_/g,"-");
	str= str.replace(/-+-/g,"-");
	str= str.replace(/^\-+|\-+$/g,""); 
	return str; 
} window.awe_convertVietnamese=awe_convertVietnamese;
function awe_category(){
	$('.nav-category .fa-angle-right').click(function(e){
		$(this).toggleClass('fa-angle-down fa-angle-right');
		$(this).parent().toggleClass('active');
	});
	$('.nav-category .fa-angle-down').click(function(e){
		$(this).toggleClass('fa-angle-right');
		$(this).parent().toggleClass('active');
	});
} window.awe_category=awe_category;

function awe_backtotop() { 
	$(window).scroll(function() {
		$(this).scrollTop() > 200 ? $('.backtop').addClass('show') : $('.backtop').removeClass('show')
	});
	$('.backtop').click(function() {
		return $("body,html").animate({
			scrollTop: 0
		}, 800), !1
	});
} window.awe_backtotop=awe_backtotop;
function awe_tab() {
	$(".e-tabs:not(.not-dqtab)").each( function(){
		$(this).find('.tabs-title li:first-child').addClass('current');
		$(this).find('.tab-content').first().addClass('current');
		$(this).find('.tabs-title li').click(function(e){
			var tab_id = $(this).attr('data-tab');
			var url = $(this).attr('data-url');
			$(this).closest('.e-tabs').find('.tab-viewall').attr('href',url);
			$(this).closest('.e-tabs').find('.tabs-title li').removeClass('current');
			$(this).closest('.e-tabs').find('.tab-content').removeClass('current');
			$(this).addClass('current');
			$(this).closest('.e-tabs').find("#"+tab_id).addClass('current');

		});    
	});
} window.awe_tab=awe_tab;
$('.dropdown-toggle').click(function() {
	$(this).parent().toggleClass('open'); 	
}); 
$('.btn-close').click(function() {
	$(this).parents('.dropdown').toggleClass('open');
}); 
$(document).on('keydown','#qty, .number-sidebar',function(e){-1!==$.inArray(e.keyCode,[46,8,9,27,13,110,190])||/65|67|86|88/.test(e.keyCode)&&(!0===e.ctrlKey||!0===e.metaKey)||35<=e.keyCode&&40>=e.keyCode||(e.shiftKey||48>e.keyCode||57<e.keyCode)&&(96>e.keyCode||105<e.keyCode)&&e.preventDefault()});
$(document).on('click','.qtyplus',function(e){
	e.preventDefault();   
	fieldName = $(this).attr('data-field'); 
	var currentVal = parseInt($('input[data-field='+fieldName+']').val());
	if (!isNaN(currentVal)) { 
		$('input[data-field='+fieldName+']').val(currentVal + 1);
	} else {
		$('input[data-field='+fieldName+']').val(0);
	}
});
$(document).on('click','.qtyminus',function(e){
	e.preventDefault(); 
	fieldName = $(this).attr('data-field');
	var currentVal = parseInt($('input[data-field='+fieldName+']').val());
	if (!isNaN(currentVal) && currentVal > 1) {          
		$('input[data-field='+fieldName+']').val(currentVal - 1);
	} else {
		$('input[data-field='+fieldName+']').val(1);
	}
});
$('.open-filters').click(function(e){
	e.stopPropagation();
	$(this).toggleClass('openf');
	$('.dqdt-sidebar').toggleClass('openf');
	$('.opacity_sidebar').toggleClass('openf');
});
$('.opacity_sidebar').click(function(e){
	$('.opacity_sidebar').removeClass('openf');
	$('.dqdt-sidebar, .open-filters').removeClass('openf')
});
$('.menubutton').click(function(e){
	e.stopPropagation();
	$('.wrapmenu_right').toggleClass('open_sidebar_menu');
	$('.opacity_menu').toggleClass('open_opacity');
});
$('.opacity_menu').click(function(e){
	$('.wrapmenu_right').removeClass('open_sidebar_menu');
	$('.opacity_menu').removeClass('open_opacity');
});
$(".menubar_pc").click(function(){ 
	$('.wrapmenu_full').slideToggle('fast');
	$('.wrapmenu_full, .cloed').toggleClass('open_menu');
	$('.dqdt-sidebar, .open-filters').removeClass('openf')
});
$(".cloed").click(function(){ 
	$(this).toggleClass('open_menu');
	$('.wrapmenu_full').slideToggle('fast');
});
$(".opacity_menu").click(function(){ 
	$('.opacity_menu').removeClass('open_opacity');
});
if ($('.dqdt-sidebar').hasClass('openf')) {
	$('.wrapmenu_full').removeClass('open_menu');
} 
$('.ul_collections li > svg').click(function(){
	$(this).parent().toggleClass('current');
	$(this).toggleClass('fa-angle-down fa-angle-right');
	$(this).next('ul').slideToggle("fast");
	$(this).next('div').slideToggle("fast");
});
$('.searchion').mouseover(function() {
	$('.searchmini input').focus();                    
})
$('.quenmk').on('click', function() {
	$('#login').toggleClass('hidden');
	$('.h_recover').slideToggle();
});
$('a[data-toggle="collapse"]').click(function(e){
	if ($(window).width() >= 767) { 
		e.preventDefault();
		e.stopPropagation();
	}    
});

$('body').click(function(event) {
	if (!$(event.target).closest('.collection-selector').length) {
		$('.list_search').css('display','none');
	};
});
/* top search */

$('.search_text').click(function(){
	$(this).next().slideToggle(200);
	$('.list_search').show();
})

$('.list_search .search_item').on('click', function (e) {
	$('.list_search').hide();

	var optionSelected = $(this);


	var title = optionSelected.text();


	$('.search_text').text(title);


	$(".search-text").focus();
	optionSelected.addClass('active').siblings().removeClass('active');

});


$('.header_search form button').click(function(e) {
	e.preventDefault();
	var textmm = $('.search-text').val();
	if (textmm != '') {
		searchCollection();
		setSearchStorage('.header_search form');
	} else {
		alert('bạn chưa nhập nội dung tìm kiếm');
	}

});

$('#mb_search').click(function(){
	$('.mb_header_search').slideToggle('fast');
});

$('.fi-title.drop-down').click(function(){
	$(this).toggleClass('opentab');
});

function searchCollection() {
	var collectionId = $('.list_search .search_item.active').attr('data-coll-id');
	var vl = $('.header form input').val();
	var searchVal = $('.header_search input[type="search"]').val();
	var url = '';
	if(collectionId == 0 || vl == '') {
		url = '/search?q='+ searchVal;
	}
	else {
		url = '/search?q=collections:'+ collectionId +' AND name:' + searchVal;

	}
	window.location=url;
}

function setSearchStorage(form_id) {
	var seach_input = $(form_id).find('.search-text').val();
	var search_collection = $(form_id).find('.list_search .search_item.active').attr('data-coll-id');
	sessionStorage.setItem('search_input', seach_input);
	sessionStorage.setItem('search_collection', search_collection);
}
function getSearchStorage(form_id) {
	var search_input_st = ''; // sessionStorage.getItem('search_input');
	var search_collection_st = ''; // sessionStorage.getItem('search_collection');
	if(sessionStorage.search_input != '') {
		search_input_st = sessionStorage.search_input;
	}
	if(sessionStorage.search_collection != '') {
		search_collection_st = sessionStorage.search_collection;
	}
	$(form_id).find('.search-text').val(search_input_st);
	$(form_id).find('.search_item[data-coll-id="'+search_collection_st+'"]').addClass('active').siblings().removeClass('active');
	var search_key = $(form_id).find('.search_item[data-coll-id="'+search_collection_st+'"]').text();
	if(search_key != ''){
		var searchVal = $('.header_search input[type="search"]').val();
		$(form_id).find('.collection-selector .search_text').text(search_key);
		$('.search_item_name').text(searchVal + " thuộc danh mục " + search_key);
	}
}
function resetSearchStorage() {
	sessionStorage.removeItem('search_input');
	sessionStorage.removeItem('search_collection');
}
$(window).load(function() {
	getSearchStorage('.header_search form');
	resetSearchStorage();
});


/*JS XEM THÊM MENU DANH MỤC SP*/
$('.xemthem').click(function(e){
	e.preventDefault();
	$('ul.ul_menu>li').css('display','block');
	$(this).hide();
	$('.thugon').show();
})
$('.thugon').click(function(e){
	e.preventDefault();
	$('ul.ul_menu>li').css('display','none');
	$(this).hide();
	$('.xemthem').show();
})
$('.ul_menu .lev-1').click(function(e){
	var lil = $('.ul_menu .lev-1').length;
	var divHeight = $('.list_menu_header').height();
	if(lil = 2){
		$('.ul_menu .ul_content_right_1').css('min-height', divHeight);
	}
});
window.onload = function(e){ 
	var lil = $('.ul_menu .lev-1').length;
	var vw = $(window).width();
	if(lil < 9 && vw < 1500 && vw > 1200){
		$('li.hidden-lgg').remove();
	}
}

/*click bộ lọc*/
$('.bolocs').click(function(e){
	e.stopPropagation();
	$('.aside-filter').slideToggle();
});
$('.aside-filter').click(function(e){
	e.stopPropagation();
});
$(document).click(function(){
	$('.aside-filter').slideUp();
});

function callback_toggle() {
	$('.bolocs').click(function(e) {
		e.stopPropagation();
		$('.aside-filter').toggleClass('show');
	});
	$('.aside-filter').click(function(e) {
		e.stopPropagation();
	});
}
if (wDWs > 992) {
	function horizontalNav() {
		return {
			wrapper: $('.navigation-horizontal'),
			navigation: $('.navigation-horizontal .nav'),
			item: $('.navigation-horizontal .nav .nav-item'),
			totalStep: 0,
			onCalcNavOverView: function(){
				let itemHeight = this.item.eq(0).outerWidth(),
					lilength = this.item.length,
					total = 0;
				for (var i = 0; i < lilength; i++) {
					itemHeight = this.item.eq(i).outerWidth();
					total += itemHeight;
				}
				return Math.ceil(total)
			},
			onCalcTotal: function(){
				let  navHeight = this.navigation.width();
				return Math.ceil(navHeight)
			},
			init:function(){
				this.totalStep = this.onCalcNavOverView();
				this.totalTo = this.onCalcTotal();
				if(this.totalStep > this.totalTo){
					this.wrapper.addClass('overflow')
				} 
			}
		}	
	}
}
$(document).ready(function ($) {
	if(window.matchMedia('(min-width: 992px)').matches){
		horizontalNav().init()
		$(window).on('resize',()=>horizontalNav().init())
		var margin_left = 0;
		$('#prev').on('click', function(e) {
			e.preventDefault();
			animateMargin(190);
		});
		$('#next').on('click', function(e) {
			e.preventDefault();
			animateMargin(-190);
		});
		const animateMargin = ( amount ) => {
			margin_left = Math.min(0, Math.max( getMaxMargin(), margin_left + amount ));
			$('ul.nav').animate({
				'margin-left': margin_left
			}, 300);
		};
		const getMaxMargin = () => 
		$('ul.nav').parent().width() - $('ul.nav')[0].scrollWidth;
	}
});

$(document).ready(function(){
	$('.header_tim_kiem .search-bar input.input-group-field, .search-mobile .search-bar input.input-group-field').focus(function(eventClick) {
		eventClick.stopPropagation();
		$('.search-suggest').addClass('open');
	});
	$(document).click( function(eventClick){
		if ( !$(eventClick.target).closest('.header_tim_kiem .search-bar, .search-mobile .search-bar').length ) {
			$('.search-suggest').removeClass('open');
		}
	});

});



$(document).ready(function(){
	function getItemSearch(name, smartjson){
		return fetch(`https://${window.location.hostname}/search?q=${name}&view=${smartjson}&type=product`)
			.then(res => res.json())
			.catch(err => console.error(err))
	}

	$('.header_tim_kiem input[type="text"], .search-mobile input[type="text"]').bind('keyup change', function(e){
		let term = $(this).val().trim();
		let data = '';
		var resultbox = '';
		if(term.length > 1) {
			async function goawaySearch() {
				data = await getItemSearch(term, 'smartjson');
				setTimeout(function(){
					var sizeData = Object.keys(data).length;
					if(sizeData > 0) {
						Object.keys(data).forEach(function(key) {
							if (data[key].compare_price != 0 ) {
								resultbox += `<a class="product-smart" href="${data[key].url}" title="${data[key].name}"><div class="image_thumb"><img width="58" height="58" class="lazyload loaded" src="${data[key].image}" data-src="${data[key].image}" alt="${data[key].name}" data-was-processed="true"></div><div class="product-info"><h3 class="product-name"><span>${data[key].name}</span></h3><div class="price-box"><span class="price">${data[key].price}</span><span class="compare-price">${data[key].compare_price}</span></div></div></a>`
							} else {
								resultbox += `<a class="product-smart" href="${data[key].url}" title="${data[key].name}"><div class="image_thumb"><img width="58" height="58" class="lazyload loaded" src="${data[key].image}" data-src="${data[key].image}" alt="${data[key].name}" data-was-processed="true"></div><div class="product-info"><h3 class="product-name"><span>${data[key].name}</span></h3><div class="price-box"><span class="price">${data[key].price}</span></div></div></a>`
							}
						});
						resultbox +=`<a href="/search?query=${term}&type=product" class="see-all-search" title="Xem tất cả">Xem tất cả</a>`
						$('.list-search').html(resultbox);
					} else {
						$('.list-search').html('<div class="not-pro">Không có thấy kết quả tìm kiếm</div>');
					}
				}, 200);
			}
			goawaySearch();
		}else {
			$('.list-search').html('');
		}
	});
});

$('.show-all-col .view-all-col').click(function(e){
	$(this).toggleClass('d-none');
	$('.show-all-col .aside-content-all').slideToggle();
	$('.aside-content-sub').slideToggle();
	$('.show-all-col .less-all-col').toggleClass('d-none');
});
$('.show-all-col .less-all-col').click(function(e){
	$(this).toggleClass('d-none');
	$('.aside-content-sub').slideToggle();
	$('.show-all-col .aside-content-all').slideToggle();
	$('.show-all-col .view-all-col').toggleClass('d-none');
});

theme.compare = (function (){
	var compareButtonClass = '.js-btn-compare',
		compareRemoveButtonClass = '.js-remove-compare',
		$compareShowButton = $('.site-header__compare'),
		$compareCount = $('.js-compare-count'),
		$compareContainer = $('.js-compare-content'),
		$compareProduct = $('.compare-product'),
		$compareSpecification = $('.compare-specification'),
		compareObject = JSON.parse(localStorage.getItem('localCompare')) || [],
		alertClass='alert-success',
		sudesCheckProductType='',
		sudesDefaultProductType='';
	function updateCompare(self) {
		var productHandle = $(self).data('handle'),
			productType = $(self).data('type'),
			productHasContent = $(self).data('content'),
			alertText = '';
		var isAdded = $.inArray(productHandle,compareObject) !== -1 ? true:false;
		sudesCheckProductType = $(self).data('type');
		if (isAdded) {
			compareObject.splice(compareObject.indexOf(productHandle), 1);
			alertText = 'Đã xóa khỏi danh sách so sánh';
			alertClass = 'alert-primary';
		}else {
			if(productHasContent == false) {
				alertText = 'Sản phẩm không có nội dung để so sánh';
				alertClass = 'alert-danger';
			}
			else{
				if(compareObject.length === 3){
					alertText = 'So sánh tối đa 3 sản phẩm';
					alertClass = 'alert-danger';
				}else{
					if(sudesDefaultProductType == ''){
						alertClass = 'alert-success';
						compareObject.push(productHandle);
						alertText = 'Đã thêm vào danh sách so sánh';
					}else{
						if(compareObject.length > 0){
							if(sudesDefaultProductType != sudesCheckProductType){
								alertText = 'Sản phẩm so sánh phải cùng loại';
								alertClass = 'alert-danger';
							}else{
								alertClass = 'alert-success';
								compareObject.push(productHandle);
								alertText = 'Đã thêm vào danh sách so sánh';
							}
						}else{
							alertClass = 'alert-success';
							compareObject.push(productHandle);
							alertText = 'Đã thêm vào danh sách so sánh';
						}
					}
				}
			}
		}
		localStorage.setItem('localCompare', JSON.stringify(compareObject)); 
		theme.alert.new('So sánh sản phẩm',alertText,3000,alertClass);
		$compareCount.text(compareObject.length);
		var sudesFirstCompareProductHandle = compareObject[0];
		Bizweb.getProduct(sudesFirstCompareProductHandle,function(product){
			sudesDefaultProductType = product.product_type;
		});
	};
	function loadCompare(){
		var compareGrid;
		//$compareContainer.html('');
		$compareProduct.html('');
		$compareSpecification.html('');
		if (compareObject.length > 0){
			$compareShowButton.removeClass('d-none');
			compareGrid = compareObject.length === 1? 'col' : 'col';
			for (var i = 0; i < compareObject.length; i++) { 
				var productHandle = compareObject[i];
				Bizweb.getProduct(productHandle,function(product){
					var htmlProduct = '', htmlSpecification = '',
						productComparePrice = Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(product.variants[0].compare_at_price),
						productAvailable = product.available ? "Còn hàng" : "Hết hàng",
						productAvailableClass = product.available ? 'alert-success' : 'alert-danger',
						productVendorHTML = product.vendor !== null ? '<a href="/collections/vendors?q='+ product.vendor +'">'+ product.vendor +'</a>' : '<span>Đang cập nhật</span>';
					if(product.featured_image != null){
						var src = Bizweb.resizeImage(product.featured_image, 'large');
					}else{
						var src = "//bizweb.dktcdn.net/thumb/large/assets/themes_support/noimage.gif";
					}
					if(product.variants[0].price > 0 ){
						var productPrice = Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(product.variants[0].price);
					}else{
						var productPrice = "Liên hệ";
					}
					if(product.content != null){
						var productContent = product.content;
						if(productContent.includes('---')){
							var productMainContent = productContent.split('---')[1];
							if(productMainContent.includes('h5')){
								var productMainContent = productMainContent.split('<h5>')[0];
								if(productMainContent.includes('h6')){
									var productMainContent = productMainContent.split('<h6>')[1];
								}else{
									var productMainContent = productMainContent.split('<h5>')[0];
								}
							}else{
								var productMainContent = productContent.split('---')[1];
							}
						}else{
							var productMainContent = "<div class='no-content'>Nội dung đang cập nhật</div>";
						}
					}else{
						var productMainContent = "<div class='no-content'>Nội dung đang cập nhật</div>";
					}
					htmlProduct += '<div class="compare-item '+compareGrid+' col-6 col-lg-3 col-md-4 padding-2"></div>';
					htmlProduct += '<div class="compare-item '+compareGrid+' col-6 col-lg-3 col-md-4 padding-2">';
					htmlProduct += '	<div class="compage-image"><span class='+ productAvailableClass +'> '+ productAvailable +'</span><button class="js-remove-compare" data-handle="'+product.alias+'" title="Xóa"><span>x</span></button>';
					htmlProduct += '	<a href="'+ product.url +'" title="'+ product.name +'">';
					htmlProduct += '		<img src="'+ src +'" alt="'+ product.name +'" />';
					htmlProduct += '	</a></div>';
					htmlProduct += '	<h5><a class="line-clamp line-clamp-2" href="'+ product.url +'" title="'+ product.name +'">'+ product.name +'</a></h5>';
					htmlProduct += '<div class="group-price">';
					if(product.variants[0].compare_at_price > product.variants[0].price ){
						htmlProduct += '	<span class="old-price">'+ productComparePrice +'</span>';
					}
					htmlProduct +='<span class="price"> '+ productPrice +'</span></div>';
					htmlProduct += '</div>';
					$compareProduct.append(htmlProduct);
					htmlSpecification += '<div class="compare-item '+compareGrid+' col-6 col-lg-3 col-md-4 padding-2">';
					htmlSpecification +=productMainContent.replace("Thông số kỹ thuật", "");
					htmlSpecification += '</div>';
					$compareSpecification.append(htmlSpecification);
					$('.compare-specification h6, .compare-specification > p,.compare-specification .compare-item > p').remove();
				});
			}
			var sudesFirstCompareProductHandle = compareObject[0];
			Bizweb.getProduct(sudesFirstCompareProductHandle,function(product){
				sudesDefaultProductType = product.product_type;
				var countcomparecell = $('.compare-specification .compare-item:nth-child(1) table tr').length;
				for (var i = 1; i <= countcomparecell; i++){
					var height1 = $(".compare-specification .compare-item:nth-child(1) table tr:nth-child("+i+")").height();
					var height2 = $(".compare-specification .compare-item:nth-child(2) table tr:nth-child("+i+")").height();
					var height3 = $(".compare-specification .compare-item:nth-child(3) table tr:nth-child("+i+")").height();
					var setHeight = Math.max(height1, height2, height3);
					$(".compare-specification .compare-item:nth-child(1) table tr:nth-child("+i+")").height(setHeight);
					$(".compare-specification .compare-item:nth-child(2) table tr:nth-child("+i+")").height(setHeight);
					$(".compare-specification .compare-item:nth-child(3) table tr:nth-child("+i+")").height(setHeight);
				}
			});
		}else{
			$compareContainer.html('<div class="alert alert-warning margin-10">Vui lòng chọn sản phẩm để so sánh</div>');
			$compareShowButton.addClass('d-none');
			sudesDefaultProductType = '';
		}
		$(compareButtonClass).each(function(){
			var productHandle = $(this).data('handle');
			var status = $.inArray(productHandle,compareObject) !== -1 ? 'added' : '';
			var statusTitle = $.inArray(productHandle,compareObject) !== -1 ? 'Bỏ so sánh' : 'Thêm vào so sánh';
			$(this).removeClass('added').addClass(status);
			$(this).attr('title', statusTitle);
			$(this).find('span.title').text(statusTitle);
		});
		$compareCount.text(compareObject.length);
	}
	$(document).on('click',compareButtonClass,function (event) {
		event.preventDefault();
		updateCompare(this);
		loadCompare();
	});
	$(document).on('click',compareRemoveButtonClass,function(){
		var productHandle = $(this).data('handle');
		compareObject.splice(compareObject.indexOf(productHandle), 1);
		localStorage.setItem('localCompare', JSON.stringify(compareObject)); 
		loadCompare();
	});
	loadCompare();
	$(document).on('Bizweb:section:load', loadCompare);
	return{
		load:loadCompare
	}
})
theme.compare();

theme.alert = (function() {
	var $alert = $('#js-global-alert'),
		$title = $('#js-global-alert .alert-heading'),
		$content = $('#js-global-alert .alert-content'),
		close = '#js-global-alert .close';
	var timeoutID = null;
	$(document).on('click', close, function() {
		$alert.removeClass('active');
	});

	function createAlert(title, mess, time, type) {
		var alertTitle = title || '',
			showTime = time || 3000,
			alertClass = type;

		$alert.removeClass('alert-success').removeClass('alert-danger').removeClass('alert-warning').removeClass('alert-primary');
		$alert.addClass(alertClass);
		$title.html(title);
		$content.html(mess);
		$alert.addClass('active');
		if (timeoutID) {
			clearTimeout(timeoutID);
		}

		timeoutID = setTimeout(function() {
			$alert.removeClass('active');
		}, showTime);
	}

	return {
		new: createAlert
	};
})();