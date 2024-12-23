package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

/**
 * 【無料会員用】サブスク登録・【有料会員用】サブスク変更・解約コントローラー
 */
@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	// サブスクリプションのStripeの価格IDを取得
    @Value("${stripe.premium-plan-price-id}")
    private String premiumPlanPriceId;

    private final UserService userService;
    private final StripeService stripeService;

    public SubscriptionController(UserService userService, StripeService stripeService) {
        this.userService = userService;
        this.stripeService = stripeService;
    }
    
    /**
     * 有料プラン登録画面の表示
     * @return	：有料プラン登録ページ
     */
    @GetMapping("/register")
    public String register() {
        return "subscription/register";
    }

    /**
     * 有料プラン登録
     * @param userDetailsImpl	：ログイン中のユーザー
     * @param paymentMethodId	：フォームから送信された支払い方法（StripeのPaymentMethodオブジェクト）
     * @param redirectAttributes
     * @return					：Stripeにサブスクリプションの登録をしてトップページにリダイレクト
     */
    @PostMapping("/create")
    public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String paymentMethodId, RedirectAttributes redirectAttributes) {
        User user = userDetailsImpl.getUser();

        // ユーザーのstripeCustomerIdフィールドがnull、つまりそのユーザーが初めてサブスクリプションに加入する場合の処理
        if (user.getStripeCustomerId() == null) {
            try {
                // 顧客（StripeのCustomerオブジェクト）を作成する
                Customer customer = stripeService.createCustomer(user);

                // stripeCustomerIdフィールドに顧客IDを保存する
                userService.saveStripeCustomerId(user, customer.getId());
            } catch (StripeException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");

                return "redirect:/";
            }
        }

        String stripeCustomerId = user.getStripeCustomerId();

        try {
            // フォームから送信された支払い方法（StripeのPaymentMethodオブジェクト）を顧客に紐づける
            stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);

            // フォームから送信された支払い方法を顧客のデフォルトの支払い方法に設定する
            stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);

            // サブスクリプション（StripeのSubscriptionオブジェクト）を作成する
            stripeService.createSubscription(stripeCustomerId, premiumPlanPriceId);

        } catch (StripeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");

            return "redirect:/";
        }

        // ユーザーのロールを更新する
        userService.updateRole(user, "ROLE_SUBSCRIPTION");
        // ログイン状態のまま更新したロールを読み込む
        userService.refreshAuthenticationByRole("ROLE_SUBSCRIPTION");

        redirectAttributes.addFlashAttribute("successMessage", "有料プランへの登録が完了しました。");

        return "redirect:/";
    }

    /**
     * 支払方法編集画面の表示
     * @param userDetailsImpl	：ログイン中のユーザー
     * @param redirectAttributes
     * @param model
     * @return					：支払方法編集ページ
     */
    @GetMapping("/edit")
    public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes, Model model) {
        User user = userDetailsImpl.getUser();

        try {
            // 顧客のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を取得する
            PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethod(user.getStripeCustomerId());

            model.addAttribute("card", paymentMethod.getCard());
            model.addAttribute("cardHolderName", paymentMethod.getBillingDetails().getName());
        } catch (StripeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法を取得できませんでした。再度お試しください。");

            return "redirect:/";
        }

        return "subscription/edit";
    }

    /**
     * 支払方法の更新
     * @param userDetailsImpl	：ログイン中のユーザー
     * @param paymentMethodId	：フォームから送信された支払い方法（StripeのPaymentMethodオブジェクト）
     * @param redirectAttributes
     * @return					：Stripeにカード情報の更新をかけてトップページにリダイレクト
     */
    @PostMapping("/update")
    public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String paymentMethodId, RedirectAttributes redirectAttributes) {
        User user = userDetailsImpl.getUser();
        String stripeCustomerId = user.getStripeCustomerId();

        try {
            // 現在のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）のIDを取得する
            String currentDefaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(stripeCustomerId);

            // フォームから送信された支払い方法を顧客（StripeのCustomerオブジェクト）に紐づける
            stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);

            // フォームから送信された支払い方法を顧客のデフォルトの支払い方法に設定する
            stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);

            // 以前のデフォルトの支払い方法と顧客の紐づけを解除する
            stripeService.detachPaymentMethodFromCustomer(currentDefaultPaymentMethodId);
        } catch (StripeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法の変更に失敗しました。再度お試しください。");

            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("successMessage", "お支払い方法を変更しました。");

        return "redirect:/";
    }
    
    /**
     * サブスクリプションの解約
     * @param userDetailsImpl	：ログイン中のユーザー
     * @param redirectAttributes
     * @return					：サブスクリプションの解約をしてトップページにリダイレクト
     */
    @PostMapping("/delete")
    public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
        User user = userDetailsImpl.getUser();

        try {
            // 顧客が契約中のサブスクリプション（StripeのSubscriptionオブジェクト）を取得する
            List<Subscription> subscriptions = stripeService.getSubscriptions(user.getStripeCustomerId());

            // 顧客が契約中のサブスクリプションをキャンセルする
            stripeService.cancelSubscriptions(subscriptions);

            // デフォルトの支払い方法（StripeのPaymentMethodオブジェクト）のIDを取得する
            String defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());

            // デフォルトの支払い方法と顧客の紐づけを解除する
            stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId);
        } catch (StripeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。");

            return "redirect:/";
        }

        // ユーザーのロールを更新する
        userService.updateRole(user, "ROLE_GENERAL");
        // ログイン状態のまま更新したロールを読み込む
        userService.refreshAuthenticationByRole("ROLE_GENERAL");

        redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");

        return "redirect:/";
    }
}
