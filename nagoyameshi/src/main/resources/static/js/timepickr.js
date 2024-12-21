flatpickr(".timepicker", {
    enableTime: true,            // 時間入力を有効化
    noCalendar: true,            // カレンダーを非表示
    dateFormat: "H:i",           // 表示フォーマット（24時間制）
    time_24hr: true,             // 24時間形式を使用
    minuteIncrement: 10          // 分単位の刻みを10に設定
});