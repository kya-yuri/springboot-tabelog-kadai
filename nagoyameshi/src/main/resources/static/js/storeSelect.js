document.addEventListener("DOMContentLoaded", function () {
    const storeSelect = document.getElementById("storeSelect");
    const selectedStoresDiv = document.getElementById("selectedStores");
    const hiddenInput = document.getElementById("stores");
    const selectedStores = new Set();

    // 隠しフィールドから選択済みカテゴリIDを読み込む
    const selectedStoreIds = hiddenInput.value.split(",").filter(id => id); // カンマ区切りでIDを配列に

    // ページ読み込み時に選択済みカテゴリを表示する
    selectedStoreIds.forEach(function(storeId) {
        const option = Array.from(storeSelect.options).find(opt => opt.value === storeId);
        if (option) {
            const storeName = option.text;

            // 既に選択されていなければ追加
            if (!selectedStores.has(storeId)) {
                selectedStores.add(storeId);

                // タグを生成
                const tag = document.createElement("div");
                tag.className = "selected-store";
                tag.dataset.storeId = storeId;
                tag.innerHTML = `<span class="remove-store nagoyameshi-select-store">${storeName} ×</span>`;

                // タグの削除ボタンのイベントを設定
                tag.querySelector(".remove-store").addEventListener("click", function () {
                    selectedStores.delete(storeId);
                    tag.remove();
                    updateHiddenInput();
                    enableOption(storeId); // 削除時にプルダウンを有効化
                });

                selectedStoresDiv.appendChild(tag);
                disableOption(storeId); // 選択した項目を無効化
            }
        }
    });

    // カテゴリを選択したときの処理
    storeSelect.addEventListener("change", function () {
        const selectedOptions = Array.from(storeSelect.selectedOptions); // 複数選択を取得
        selectedOptions.forEach(function (selectedOption) {
            const storeId = selectedOption.value;
            const storeName = selectedOption.text;

            // 既に選択済みでなければ追加
            if (!selectedStores.has(storeId)) {
                selectedStores.add(storeId);

                // タグを生成
                const tag = document.createElement("div");
                tag.className = "selected-store";
                tag.dataset.storeId = storeId;
                tag.innerHTML = `<span class="remove-store nagoyameshi-select-store">${storeName} ×</span>`;

                // タグの削除ボタンのイベントを設定
                tag.querySelector(".remove-store").addEventListener("click", function () {
                    selectedStores.delete(storeId);
                    tag.remove();
                    updateHiddenInput();
                    enableOption(storeId); // 削除時にプルダウンを有効化
                });

                selectedStoresDiv.appendChild(tag);
                disableOption(storeId); // 選択した項目を無効化
                updateHiddenInput();
            }
        });

        // プルダウンを初期状態に戻す
        storeSelect.selectedIndex = -1; // 初期状態に戻す
    });

    // 隠しフィールドを更新
    function updateHiddenInput() {
        hiddenInput.value = Array.from(selectedStores).join(",");
    }

    // 指定したカテゴリIDをプルダウンで無効化
    function disableOption(storeId) {
        Array.from(storeSelect.options).forEach(option => {
            if (option.value === storeId) {
                option.disabled = true;
                option.style.backgroundColor = "#e9ecef"; // 背景色を変更
                option.style.color = "#6c757d";           // テキスト色を薄く
            }
        });
    }

    // 指定したカテゴリIDをプルダウンで有効化
    function enableOption(storeId) {
        Array.from(storeSelect.options).forEach(option => {
            if (option.value === storeId) {
                option.disabled = false;
                option.style.backgroundColor = ""; // 背景色を元に戻す
                option.style.color = "";           // テキスト色を元に戻す
            }
        });
    }
});
