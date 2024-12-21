document.addEventListener("DOMContentLoaded", function () {
    const categorySelect = document.getElementById("categorySelect");
    const selectedCategoriesDiv = document.getElementById("selectedCategories");
    const hiddenInput = document.getElementById("categories");
    const selectedCategories = new Set();

    // 隠しフィールドから選択済みカテゴリIDを読み込む
    const selectedCategoryIds = hiddenInput.value.split(",").filter(id => id); // カンマ区切りでIDを配列に

    // ページ読み込み時に選択済みカテゴリを表示する
    selectedCategoryIds.forEach(function(categoryId) {
        const option = Array.from(categorySelect.options).find(opt => opt.value === categoryId);
        if (option) {
            const categoryName = option.text;

            // 既に選択されていなければ追加
            if (!selectedCategories.has(categoryId)) {
                selectedCategories.add(categoryId);

                // タグを生成
                const tag = document.createElement("div");
                tag.className = "selected-category";
                tag.dataset.categoryId = categoryId;
                tag.innerHTML = `<span class="remove-category nagoyameshi-category-badge">${categoryName} ×</span>`;

                // タグの削除ボタンのイベントを設定
                tag.querySelector(".remove-category").addEventListener("click", function () {
                    selectedCategories.delete(categoryId);
                    tag.remove();
                    updateHiddenInput();
                    enableOption(categoryId); // 削除時にプルダウンを有効化
                });

                selectedCategoriesDiv.appendChild(tag);
                disableOption(categoryId); // 選択した項目を無効化
            }
        }
    });

    // カテゴリを選択したときの処理
    categorySelect.addEventListener("change", function () {
        const selectedOptions = Array.from(categorySelect.selectedOptions); // 複数選択を取得
        selectedOptions.forEach(function (selectedOption) {
            const categoryId = selectedOption.value;
            const categoryName = selectedOption.text;

            // 既に選択済みでなければ追加
            if (!selectedCategories.has(categoryId)) {
                selectedCategories.add(categoryId);

                // タグを生成
                const tag = document.createElement("div");
                tag.className = "selected-category";
                tag.dataset.categoryId = categoryId;
                tag.innerHTML = `<span class="remove-category nagoyameshi-category-badge">${categoryName} ×</span>`;

                // タグの削除ボタンのイベントを設定
                tag.querySelector(".remove-category").addEventListener("click", function () {
                    selectedCategories.delete(categoryId);
                    tag.remove();
                    updateHiddenInput();
                    enableOption(categoryId); // 削除時にプルダウンを有効化
                });

                selectedCategoriesDiv.appendChild(tag);
                disableOption(categoryId); // 選択した項目を無効化
                updateHiddenInput();
            }
        });

        // プルダウンを初期状態に戻す
        categorySelect.selectedIndex = -1; // 初期状態に戻す
    });

    // 隠しフィールドを更新
    function updateHiddenInput() {
        hiddenInput.value = Array.from(selectedCategories).join(",");
    }

    // 指定したカテゴリIDをプルダウンで無効化
    function disableOption(categoryId) {
        Array.from(categorySelect.options).forEach(option => {
            if (option.value === categoryId) {
                option.disabled = true;
                option.style.backgroundColor = "#e9ecef"; // 背景色を変更
                option.style.color = "#6c757d";           // テキスト色を薄く
            }
        });
    }

    // 指定したカテゴリIDをプルダウンで有効化
    function enableOption(categoryId) {
        Array.from(categorySelect.options).forEach(option => {
            if (option.value === categoryId) {
                option.disabled = false;
                option.style.backgroundColor = ""; // 背景色を元に戻す
                option.style.color = "";           // テキスト色を元に戻す
            }
        });
    }
});
