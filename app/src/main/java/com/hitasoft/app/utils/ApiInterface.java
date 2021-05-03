package com.hitasoft.app.utils;

import com.hitasoft.app.model.AdHistoryResponse;
import com.hitasoft.app.model.AddAddressResponse;
import com.hitasoft.app.model.AddProductResponse;
import com.hitasoft.app.model.AdminDataResponse;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.model.CategoryResponse;
import com.hitasoft.app.model.ChatResponse;
import com.hitasoft.app.model.CheckPromotionResponse;
import com.hitasoft.app.model.CommentsResponse;
import com.hitasoft.app.model.ExchangeResponse;
import com.hitasoft.app.model.FollowersResponse;
import com.hitasoft.app.model.GetAdResponse;
import com.hitasoft.app.model.GetCountResponse;
import com.hitasoft.app.model.HelpResponse;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.model.InsightsResponse;
import com.hitasoft.app.model.LoginResponse;
import com.hitasoft.app.model.MessagesResponse;
import com.hitasoft.app.model.MyPromotionResponse;
import com.hitasoft.app.model.MySalesResponse;
import com.hitasoft.app.model.NotificationResponse;
import com.hitasoft.app.model.ProfileResponse;
import com.hitasoft.app.model.PromotionResponse;
import com.hitasoft.app.model.ReviewResponse;
import com.hitasoft.app.model.SendCommentResponse;
import com.hitasoft.app.model.ShippingAddressRes;
import com.hitasoft.app.model.TrackingResponse;
import com.hitasoft.app.model.UpdateOfferResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("admindatas")
    Call<AdminDataResponse> getAdminData(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("signup")
    Call<HashMap<String, String>> signUp(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> logIn(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("sociallogin")
    Call<LoginResponse> socialLogIn(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("profile")
    Call<ProfileResponse> getProfileInformation(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("Editprofile")
    Call<ProfileResponse> editProfile(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("changepassword")
    Call<HashMap<String, String>> changePassword(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("forgetpassword")
    Call<HashMap<String, String>> forgotPassword(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("adddeviceid")
    Call<ResponseBody> addDeviceId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("pushsignout")
    Call<ResponseBody> removeDeviceId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getitems")
    Call<HomeItemResponse> getHomeItems(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getuserproducts")
    Call<HomeItemResponse> getUserProducts(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("getcategory")
    Call<CategoryResponse> getCategories(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getcountdetails")
    Call<GetCountResponse> getCountDetails(@FieldMap HashMap<String, String> apiRequest);

    @FormUrlEncoded
    @POST("Followuser")
    Call<HashMap<String, String>> followUser(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Unfollowuser")
    Call<HashMap<String, String>> unFollowUser(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Getfollowerid")
    Call<ResponseBody> getFollowingId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Getlikedid")
    Call<ResponseBody> getLikedId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Followersdetails")
    Call<FollowersResponse> getFollowersDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Followingdetails")
    Call<FollowersResponse> getFollowingDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("myexchanges")
    Call<ExchangeResponse> getExchanges(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getchatid")
    Call<HashMap<String, String>> getChatId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getchat")
    Call<ChatResponse> getChat(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("sendchat")
    Call<HashMap<String, String>> sendChat(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("exchangestatus")
    Call<HashMap<String, String>> changeExchangeStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("searchbyitem")
    Call<HomeItemResponse> getItemData(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("updateview")
    Call<HashMap<String, String>> updateView(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("solditem")
    Call<HashMap<String, String>> changeSoldStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("deleteproduct")
    Call<HashMap<String, String>> deleteProduct(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Sendofferreq")
    Call<HashMap<String, String>> createOffer(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("checkItemstatus")
    Call<HashMap<String, String>> checkItemStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("itemlike")
    Call<HashMap<String, String>> likeItem(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("reportitem")
    Call<HashMap<String, String>> reportItem(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getShippingAddress")
    Call<ShippingAddressRes> getShippingAddress(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("addshipping")
    Call<AddAddressResponse> addShippingAddress(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Chataction")
    Call<HashMap<String, String>> updateBlockStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("SafetyTips")
    Call<HashMap<String, String>> getSafetyTips(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("createexchange")
    Call<HashMap<String, String>> createExchange(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("removeshipping")
    Call<HashMap<String, String>> removeShipping(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("setdefaultshipping")
    Call<HashMap<String, String>> setDefaultShipping(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("productbeforeadd")
    Call<BeforeAddResponse> getProductBeforeAdd(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("addproduct")
    Call<AddProductResponse> addProduct(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getpromotion")
    Call<PromotionResponse> getPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("processingPayment")
    Call<HashMap<String, String>> payForPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("braintreeClientToken")
    Call<HashMap<String, String>> getClientToken(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("mypromotions")
    Call<MyPromotionResponse> getMyPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Checkpromotion")
    Call<CheckPromotionResponse> checkPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getcomments")
    Call<CommentsResponse> getComments(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("postcomment")
    Call<SendCommentResponse> postComment(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("deletecomment")
    Call<HashMap<String, String>> deleteComment(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("messages")
    Call<MessagesResponse> getMessages(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("helppage")
    Call<HelpResponse> getHelpList(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("notification")
    Call<NotificationResponse> getNotifications(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("offerstatus")
    Call<UpdateOfferResponse> updateOfferStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("updatereview")
    Call<HashMap<String, String>> updateReview(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("orderstatus")
    Call<HashMap<String, String>> addShippingDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("gettrackdetails")
    Call<TrackingResponse> getTrackDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("mysales")
    Call<MySalesResponse> getMySales(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("myorders")
    Call<MySalesResponse> getMyOrders(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getreview")
    Call<ReviewResponse> getReview(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("buynowPayment")
    Call<HashMap<String, String>> buyNowPayment(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getadwithus")
    Call<GetAdResponse> getAdWithUs(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("banneravailability")
    Call<ResponseBody> checkDateAvailability(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("addbanner")
    Call<HashMap<String, String>> addBanner(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("getadhistory")
    Call<AdHistoryResponse> getAdHistory(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("getinsights")
    Call<InsightsResponse> getInsights(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("addstripedetails")
    Call<ResponseBody> addStripeDetails(@FieldMap HashMap<String, String> hashMap);

}