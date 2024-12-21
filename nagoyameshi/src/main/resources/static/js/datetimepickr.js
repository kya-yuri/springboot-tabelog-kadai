document.addEventListener("DOMContentLoaded", function () {
	const datetimeInput = document.querySelector("#reserveDateTime");
	if (!datetimeInput) {
		console.error("The element with id '#reserveDateTime' is not found.");
		return;
	}
	const openHour = document.getElementById("openHour").textContent;
	const closedHour = document.getElementById("closedHour").textContent; 
	
	flatpickr("#reserveDateTime", {
		enableTime: true,
		dateFormat: "Y-m-d H:i",
		time_24hr: true,
		minuteIncrement: 10,
		minDate: "today", // 当日
		maxDate: new Date().fp_incr(60), // 2か月先
		disableMobile: true, // モバイル用カスタムUIを無効化
		onReady: function (selectedDates, dateStr, instance) {
			const [openHours, openMinutes] = openHour.split(":").map(Number);
			const [closedHours, closedMinutes] = closedHour.split(":").map(Number);
	
			instance.set({
				minTime: `${openHours}:${openMinutes < 10 ? '0' : ''}${openMinutes}`,
				maxTime: `${closedHours}:${closedMinutes < 10 ? '0' : ''}${closedMinutes}`,
			});
		},
	});
});