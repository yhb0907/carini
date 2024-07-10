(function($) {

	var $window = $(window),
		$body = $('body'),
		settings = {
			// Carousels
			carousels: {
				speed: 2, // Speed for smoother movement
				fadeIn: true,
				fadeDelay: 100, // Faster fade-in
				autoScroll: true, // Auto-scroll feature
				autoScrollInterval: 20 // Smaller interval for smoother scrolling
			},
		};

	// Breakpoints.
	breakpoints({
		wide: ['1281px', '1680px'],
		normal: ['961px', '1280px'],
		narrow: ['841px', '960px'],
		narrower: ['737px', '840px'],
		mobile: [null, '736px']
	});

	// Play initial animations on page load.
	$window.on('load', function() {
		window.setTimeout(function() {
			$body.removeClass('is-preload');
		}, 100);
	});

	// Dropdowns.
	$('#nav > ul').dropotron({
		mode: 'fade',
		speed: 350,
		noOpenerFade: true,
		alignment: 'center'
	});

	// Scrolly.
	$('.scrolly').scrolly();

	// Nav.

	// Button.
	$(
		'<div id="navButton">' +
		'<a href="#navPanel" class="toggle"></a>' +
		'</div>'
	).appendTo($body);

	// Panel.
	$(
		'<div id="navPanel">' +
		'<nav>' +
		$('#nav').navList() +
		'</nav>' +
		'</div>'
	).appendTo($body).panel({
		delay: 500,
		hideOnClick: true,
		hideOnSwipe: true,
		resetScroll: true,
		resetForms: true,
		target: $body,
		visibleClass: 'navPanel-visible'
	});

	// Carousels.
	$('.carousel').each(function() {

		var $t = $(this),
			$reel = $t.children('.reel'),
			$items = $reel.children('article');

		// 캐로셀 무한루프
		$(document).ready(function() {
			var $carousel = $('.carousel');
			var $reel = $carousel.find('.reel');
			var $items = $reel.children('article');

			// 아이템을 복제하여 무한 루프 효과 만들기
			var $clone = $reel.clone(); // 클론 생성
			$reel.parent().append($clone); // 클론을 원본 뒤에 추가

			var pos = 0;
			var reelWidth = $reel[0].scrollWidth; // 전체 너비 계산
			var itemWidth = $items.outerWidth(true); // 항목 너비 계산

			// 위치 업데이트 함수
			function updatePos() {
				pos -= 1; // 스크롤 속도 조정
				if (pos <= -reelWidth) { // 끝에 도달하면 위치 재설정
					pos = 0;
				}
				$reel.css('transform', 'translateX(' + pos + 'px)');
				$clone.css('transform', 'translateX(' + (pos + reelWidth) + 'px)');
				requestAnimationFrame(updatePos); // 애니메이션 프레임 요청
			}

			// 초기 위치 업데이트 호출
			updatePos();
		});


		// 캐로셀 무한루프
		$(document).ready(function() {
			var $carousel = $('.carousel');
			var $reel = $carousel.find('.reel');
			var $items = $reel.children('article');

			// 아이템을 복제하여 무한 루프 효과 만들기
			var $clone = $reel.clone();
			$reel.parent().append($clone);

			var pos = 0;
			var reelWidth = $reel.width();
			var itemWidth = $items.outerWidth(true);

			function updatePos() {
				pos -= 1; // 스크롤 속도 조정
				if (pos <= -reelWidth) {
					pos = 0;
				}
				$reel.css('transform', 'translateX(' + pos + 'px)');
				$clone.css('transform', 'translateX(' + (pos + reelWidth) + 'px)');
				requestAnimationFrame(updatePos);
			}

			// 초기 위치 업데이트 호출
			updatePos();
		});





		// 캐로셀 무한루프
		$(document).ready(function() {
			var $carousel = $('.carousel');
			var $reel = $carousel.find('.reel');
			var $items = $reel.children('article');

			// 아이템을 복제하여 무한 루프 효과 만들기
			var $clone = $reel.clone();
			$reel.parent().append($clone);

			var pos = 0;
			var reelWidth = $reel.width();
			var itemWidth = $items.outerWidth(true);

			function updatePos() {
				pos -= 1; // 스크롤 속도 조정
				if (pos <= -reelWidth) {
					pos = 0;
				}
				$reel.css('transform', 'translateX(' + pos + 'px)');
				$clone.css('transform', 'translateX(' + (pos + reelWidth) + 'px)');
				requestAnimationFrame(updatePos);
			}

			// 초기 위치 업데이트 호출
			updatePos();
		});




		// Items.
		if (settings.carousels.fadeIn) {

			$items.addClass('loading');

			$t.scrollex({
				mode: 'middle',
				top: '-20vh',
				bottom: '-20vh',
				enter: function() {

					var timerId,
						limit = $items.length - Math.ceil($window.width() / itemWidth);

					timerId = window.setInterval(function() {
						var x = $items.filter('.loading'),
							xf = x.first();

						if (x.length <= limit) {
							window.clearInterval(timerId);
							$items.removeClass('loading');
							return;
						}

						xf.removeClass('loading');

					}, settings.carousels.fadeDelay);

				}
			});

		}

		// Main.
		$t._update = function() {
			pos = 0;
			reelWidth = $reel[0].scrollWidth;
			itemWidth = $items.outerWidth(true); // Get the width of each item including margin
			$t._updatePos();
		};

		$t._updatePos = function() {
			$reel.css('transform', 'translate(' + pos + 'px, 0)');
		};

		// Auto-Scroll
		function startAutoScroll() {
			timerId = setInterval(function() {
				pos -= settings.carousels.speed;
				if (pos <= -reelWidth) {
					pos = 0; // 무한 스크롤 효과를 위해 위치 재설정
				}
				$t._updatePos();
			}, settings.carousels.autoScrollInterval);
		}

		// Init.
		$window.on('load', function() {
			$t._update();
			startAutoScroll();

			$window.on('resize', function() {
				$t._update();
			}).trigger('resize');
		});

	});

})(jQuery);


// 배너 페이드인 / 페이드아웃
$(document).ready(function() {
	$(window).scroll(function() {

		$('.banner').each(function(i) {

			var bottom_of_element = $(this).offset().top + $(this).outerHeight() / 3;
			var bottom_of_window = $(window).scrollTop() + $(window).height();

			if (bottom_of_window > bottom_of_element) {
				$(this).animate({ 'opacity': '1' }, 1000);
			} else {
				$(this).animate({ 'opacity': '0' }, 10);
			}

		});
	});
});

// arrow기능
document.getElementById('arrow_png').addEventListener('click', function() {

   window.scrollTo({
      top: 820,
      behavior: 'smooth'
   });
});

// QR버튼 삭제 기능
document.getElementById('delete_button').addEventListener('click', function() {
	const delete_button = document.getElementById('qr_section');
	delete_button.style.display = 'none'
});

//  하단 qr-section 나타나는 기능
$(document).ready(function() {

	$(window).scroll(function() {

		var scrollPosition = $(this).scrollTop();

		var qrSectionTop = $('#qr_section').offset().top;


		var showPosition = 1800;


		if (scrollPosition > showPosition) {
			$('#qr_section').css({
				'opacity': '0.9',
				'transform': 'translateY(-50%)'
			});
		} else {

			$('#qr_section').css({
				'opacity': '0',
				'transform': 'translateY(0)'
			});
		}
	});
});


