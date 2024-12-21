document.addEventListener('DOMContentLoaded', function () {
    const scrollContainer = document.querySelector('.nagoyameshi-scroll-images');
    const images = Array.from(scrollContainer.children);

    // ランダムに並び替えて20枚を取得
    const selectedImages = images
        .sort(() => Math.random() - 0.5) // ランダムソート
        .slice(0, 20); // 最初の20枚を取得

    // コンテナをクリアして選択した画像を追加
    scrollContainer.innerHTML = "";
    selectedImages.forEach(img => scrollContainer.appendChild(img));

    // 必要に応じてリストを複製してループを作成
    const containerWidth = scrollContainer.offsetWidth;
    const originalContentWidth = scrollContainer.scrollWidth;

    while (scrollContainer.scrollWidth < containerWidth * 2) {
        selectedImages.forEach(img => {
            const clone = img.cloneNode(true);
            scrollContainer.appendChild(clone);
        });
    }

    // スクロール速度を時間で一定に保つ設定
    const scrollSpeed = 100; // 1秒間に移動するピクセル数
    let startTime;

    function step(timestamp) {
        if (!startTime) startTime = timestamp;
        const elapsed = timestamp - startTime;

        // スクロール位置の計算（時間に基づく一定速度）
        const offset = (elapsed / 1000) * scrollSpeed;

        // 全体の長さに応じてシームレスなループを実現
        scrollContainer.style.transform = `translateX(${-offset % originalContentWidth}px)`;

        requestAnimationFrame(step);
    }

    requestAnimationFrame(step);
});
