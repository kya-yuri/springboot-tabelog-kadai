package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {
	// Stripeのシークレットキーを取得
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    // 依存性の注入後に一度だけ実行するメソッド
    @PostConstruct
    private void init() {
        // Stripeのシークレットキーを設定する
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * 顧客（StripeのCustomerオブジェクト）を作成する
     * @param 					：ログイン中のユーザーの Userエンティティ
     * @return					：Customerオブジェクト
     * @throws StripeException	：Stripeの例外処理
     */
    public Customer createCustomer(User user) throws StripeException {
        // 顧客の作成時に渡すユーザーの情報
        CustomerCreateParams customerCreateParams =
            CustomerCreateParams.builder()
                .setName(user.getName())
                .setEmail(user.getEmail())
                .build();

        return Customer.create(customerCreateParams);
    }

    /**
     * 支払い方法（StripeのPaymentMethodオブジェクト）を顧客（StripeのCustomerオブジェクト）に紐づける
     * @param paymentMethodId	：支払い方法のID
     * @param customerId		：顧客ID
     * @throws StripeException	：Stripeの例外処理
     */
    public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
        // 支払い方法を紐づける顧客
        PaymentMethodAttachParams paymentMethodAttachParams =
            PaymentMethodAttachParams.builder()
            .setCustomer(customerId)
            .build();
        
        // 支払い方法のIDをもとに PaymentMethodオブジェクトを取得
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        // PaymentMethodオブジェクトを顧客に紐づける
        paymentMethod.attach(paymentMethodAttachParams);
    }

    /**
     * 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を設定する
     * @param paymentMethodId	：支払い方法のID
     * @param customerId		：顧客ID
     * @throws StripeException	：Stripeの例外処理
     */
    public void setDefaultPaymentMethod(String paymentMethodId, String customerId) throws StripeException {
        // 顧客の更新時に渡すデフォルトの支払い方法のID
        CustomerUpdateParams customerUpdateParams =
            CustomerUpdateParams.builder()
                .setInvoiceSettings(
                    CustomerUpdateParams.InvoiceSettings.builder()
                        .setDefaultPaymentMethod(paymentMethodId)
                        .build()
                )
                .build();
        
        // Customerオブジェクトを更新する
        Customer customer = Customer.retrieve(customerId);
        customer.update(customerUpdateParams);
    }

    /**
     * サブスクリプション（StripeのSubscriptionオブジェクト）を作成する
     * @param customerId		：顧客ID
     * @param priceId			：価格ID
     * @return					：SubscriptionCreateParamsオブジェクト
     * @throws StripeException	：Stripeの例外処理
     */
    public Subscription createSubscription(String customerId, String priceId) throws StripeException {
        // サブスクリプションの作成時に渡す顧客IDや価格ID
        SubscriptionCreateParams subscriptionCreateParams =
            SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(
                    SubscriptionCreateParams
                      .Item.builder()
                      .setPrice(priceId)
                      .build()
                )
                .build();

        return Subscription.create(subscriptionCreateParams);
    }

    /**
     * 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を取得する
     * @param customerId		：顧客ID
     * @return					：PaymentMethodオブジェクト
     * @throws StripeException	：Stripeの例外処理
     */
    public PaymentMethod getDefaultPaymentMethod(String customerId) throws StripeException {
        // Customerオブジェクトからデフォルトの支払い方法のIDを取得する
    	Customer customer = Customer.retrieve(customerId);
        String defaultPaymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();
        return PaymentMethod.retrieve(defaultPaymentMethodId);
    }

    /**
     * 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）のIDを取得する
     * @param customerId		：顧客ID
     * @return					：PaymentMethodオブジェクト
     * @throws StripeException	：Stripeの例外処理
     */
    public String getDefaultPaymentMethodId(String customerId) throws StripeException {
        Customer customer = Customer.retrieve(customerId);
        // Customerオブジェクトからデフォルトの支払い方法のIDを取得し、戻り値として返す
        return customer.getInvoiceSettings().getDefaultPaymentMethod();
    }

    /**
     * 支払い方法（StripeのPaymentMethodオブジェクト）と顧客（StripeのCustomerオブジェクト）の紐づけを解除する
     * @param paymentMethodId	：支払い方法のID
     * @throws StripeException	：Stripeの例外処理
     */
    public void detachPaymentMethodFromCustomer(String paymentMethodId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        // PaymentMethodオブジェクトの紐づけを解除する
        paymentMethod.detach();
    }

    /**
     * サブスクリプション（StripeのSubscriptionオブジェクト）を取得する
     * @param customerId		：顧客ID
     * @return					：Subscriptionオブジェクトのリスト
     * @throws StripeException	：Stripeの例外処理
     */
    public List<Subscription> getSubscriptions(String customerId) throws StripeException {
        // 契約中のサブスクリプションの取得時に渡す顧客ID
        SubscriptionListParams subscriptionListParams =
            SubscriptionListParams.builder()
                .setCustomer(customerId)
                .build();

        // Subscriptionオブジェクトのリストを取得し、戻り値として返す
        return Subscription.list(subscriptionListParams).getData();
    }

    /**
     *  サブスクリプション（StripeのSubscriptionオブジェクト）をキャンセルする
     * @param subscriptions		：Subscriptionオブジェクトのリスト
     * @throws StripeException	：Stripeの例外処理
     */
    public void cancelSubscriptions(List<Subscription> subscriptions) throws StripeException {
    	// リストの要素の数だけ繰り返し処理を行い、その要素をキャンセルする
        for (Subscription subscription : subscriptions) {
            subscription.cancel();
        }
    }
}
