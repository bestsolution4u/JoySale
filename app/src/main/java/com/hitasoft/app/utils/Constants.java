package com.hitasoft.app.utils;/**************** * * @author 'Hitasoft Technologies' * * Description: * This class is used for storing static data. * * Revision History: * Version 1.0 - Initial Version * *****************/import android.content.SharedPreferences;import android.content.SharedPreferences.Editor;import static android.Manifest.permission.ACCESS_COARSE_LOCATION;import static android.Manifest.permission.ACCESS_FINE_LOCATION;import static android.Manifest.permission.CAMERA;import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;public class Constants {    /**     * App Urls     **/    public static final String SITE_URL = "http://173.249.1.40/";    public static final String URL = SITE_URL + "api/";    public static final String SOCKET_URL = "http://173.249.1.40:8085";    public static final String API_UPLOAD_IMAGE = URL + "uploadimage";    /**     * General Setting     **/    public static final int ITEM_LIMIT = 20;    public static final String GOOGLE_MAPS_API = "https://maps.google.com/maps/api/";    public static SharedPreferences pref, filpref;    public static Editor editor, fileditor;    public static String REGISTER_ID = "";    public static String ANDROID_ID = "";    //Product upload Image Count    // Home page banner image size    // For choosing default language    // For Enable or Disable Giving away Module    // For Enable or Disable Promotion Module    // For Enable or Disable Exchange Module    // For Enable or Disable BuyNow Module    public static boolean BUYNOW = false;    public static boolean EXCHANGE = true;    public static boolean PROMOTION = true;    public static boolean GIVINGAWAY = true;    public static boolean PAIDBANNER = false;    public static boolean SITEMAINTAIN = false;    public static String LANGUAGE = "English";    public static String LANGUAGE_CODE = "en";    public static int HOME_BANNER_WIDTH = 1024, HOME_BANNER_HEIGHT = 500;    public static int IMAGE_COUNT = 5;    /**     * login details to access json from api     **/    public static final String SOAP_USERNAME = "api_username";    public static final String SOAP_PASSWORD = "api_password";    public static final String SOAP_USERNAME_VALUE = "joySale";    public static final String SOAP_PASSWORD_VALUE = "0RWK9XM8";    /**     * Static Keywords for Json Parsing     **/    public static final String TAG_TRUE = "true";    public static final String TAG_STATUS = "status";    public static final String TAG_RESULT = "result";    public static final String TAG_ITEMS = "items";    public static final String TAG_ID = "id";    public static final String TAG_TITLE = "item_title";    public static final String TAG_PROURL = "product_url";    public static final String TAG_ITEM_DES = "item_description";    public static final String TAG_ITEM_CONDITION = "item_condition";    public static final String TAG_PRICE = "price";    public static final String TAG_FORMATTED_PRICE = "formatted_price";    public static final String TAG_SELLERID = "seller_id";    public static final String TAG_SELLERNAME = "seller_name";    public static final String TAG_SELLERIMG = "seller_img";    public static final String TAG_LIKECOUNT = "likes_count";    public static final String TAG_BUYTYPE = "buy_type";    public static final String TAG_COMMENTCOUNT = "comments_count";    public static final String TAG_VIEWCOUNT = "views_count";    public static final String TAG_LIKED = "liked";    public static final String TAG_POSTED_TIME = "posted_time";    public static final String TAG_LATITUDE = "latitude";    public static final String TAG_LONGITUDE = "longitude";    public static final String TAG_BEST_OFFER = "best_offer";    public static final String TAG_PHOTOS = "photos";    public static final String TAG_PHOTO = "photo";    public static final String TAG_ITEM_URL_350 = "item_url_main_350";    public static final String TAG_ITEM_URL_ORG = "item_url_main_original";    public static final String TAG_COMMENTID = "comment_id";    public static final String TAG_COMMENT = "comment";    public static final String TAG_USERID = "user_id";    public static final String TAG_FCM_USERID = "userid";    public static final String TAG_USERIMG = "user_img";    public static final String TAG_USERNAME = "user_name";    public static final String TAG_COMMENTTIME = "comment_time";    public static final String TAG_QUANTITY = "quantity";    public static final String TAG_COLOR = "color";    public static final String TAG_LOCATION = "location";    public static final String TAG_LOCATION_REMOVED = "location_removed";    public static final String TAG_ITEM_STATUS = "item_status";    public static final String TAG_LAST_REPLIED = "last_repliedto";    public static final String TAG_SUBCATEGORYID = "subcat_id";    public static final String TAG_SUBCATEGORYNAME = "subcat_name";    public static final String TAG_SHIPPING_DETAIL = "shipping_detail";    public static final String TAG_PAYPALID = "paypal_id";    public static final String TAG_REPORT = "report";    public static final String TAG_WIDTH = "width";    public static final String TAG_HEIGHT = "height";    public static final String TAG_PROMOTION_TYPE = "promotion_type";    public static final String TAG_EXCHANGE_BUY = "exchange_buy";    public static final String TAG_MAKE_OFFER = "make_offer";    public static final String TAG_SELLER_USERNAME = "seller_username";    public static final String TAG_FACEBOOK_VERIFICATION = "facebook_verification";    public static final String TAG_MOBILE_VERIFICATION = "mobile_verification";    public static final String TAG_EMAIL_VERIFICATION = "email_verification";    public static final String TAG_BUYER_USERNAME = "buyer_username";    public static final String TAG_BUYERNAME = "buyer_name";    public static final String TAG_BUYERID = "buyer_id";    public static final String TAG_BUYERIMG = "buyer_img";    public static final String TAG_MOBILE_NO = "mobile_no";    public static final String TAG_SHOW_SELLER_MOB = "show_seller_mobile";    public static final String TAG_SELLER_RATING = "seller_rating";    public static final String TAG_RATING_USER_COUNT = "rating_user_count";    public static final String TAG_VERIFICATION = "verification";    public static final String TAG_GIVING_AWAY = "giving_away";    public static final String TAG_USERIMAGE = "user_image";    public static final String TAG_ITEMIMAGE = "item_image";    public static final String TAG_MESSAGEID = "message_id";    public static final String TAG_MESSAGETIME = "message_time";    public static final String TAG_EXCHANGE = "exchange";    public static final String TAG_EXCHANGEID = "exchange_id";    public static final String TAG_EXCHANGETIME = "exchange_time";    public static final String TAG_EXCHANGERNAME = "exchanger_name";    public static final String TAG_EXCHANGERUSERNAME = "exchanger_username";    public static final String TAG_EXCHANGERID = "exchanger_id";    public static final String TAG_EXCHANGERIMG = "exchanger_image";    public static final String TAG_REQUEST_BY_ME = "request_by_me";    public static final String TAG_MYPRODUCT = "my_product";    public static final String TAG_EXCHANGEPRODUCT = "exchange_product";    public static final String TAG_CATEGORY = "category";    public static final String TAG_CATEGORY_LIST = "category_list";    public static final String TAG_CATEGORYID = "category_id";    public static final String TAG_CATEGORYNAME = "category_name";    public static final String TAG_CATEGORYIMG = "category_img";    public static final String TAG_SUBCATEGORY = "subcategory";    public static final String TAG_SUBID = "sub_id";    public static final String TAG_SUBNAME = "sub_name";    public static final String TAG_URGENT = "urgent";    public static final String TAG_AD = "ad";    public static final String TAG_CURRENCY_CODE = "currency_code";    public static final String TAG_CURRENCY_SYMBOL = "currency_symbol";    public static final String TAG_CURRENCY_MODE = "currency_mode";    public static final String TAG_CURRENCY_POSITION = "currency_position";    public static final String TAG_DAYS = "days";    public static final String TAG_TOKEN = "token";    public static final String TAG_PROMOTION_NAME = "promotion_name";    public static final String TAG_PAID_AMOUNT = "paid_amount";    public static final String TAG_UPTO = "upto";    public static final String TAG_TRANSACTION_ID = "transaction_id";    public static final String TAG_ITEM_IMAGE = "item_image";    public static final String TAG_ITEM_ID = "item_id";    public static final String TAG_CHAT_ID = "chat_id";    public static final String TAG_CHAT = "chat";    public static final String TAG_RECEIVER = "receiver";    public static final String TAG_SENDER = "sender";    public static final String TAG_CHATTIME = "chatTime";    public static final String TAG_CHAT_URL = "chat_url";    public static final String TAG_OFFER_PRICE = "offer_price";    public static final String TAG_OFFER_ID = "offer_id";    public static final String TAG_OFFER_SHIPPING = "offer_shipping_price";    public static final String TAG_OFFER_TYPE = "offer_type";    public static final String TAG_OFFER_STATUS = "offer_status";    public static final String TAG_BUYNOW_STATUS = "buynow_status";    public static final String TAG_CHAT_TEMPLATE = "chat_template";    public static final String TAG_TYPE = "type";    public static final String TAG_EVENTTIME = "event_time";    public static final String TAG_MESSAGE = "message";    public static final String TAG_ITEMID = "item_id";    public static final String TAG_FULL_NAME = "full_name";    public static final String TAG_SHIPPING_TIME = "shipping_time";    public static final String TAG_SHIPPING_COST = "shipping_cost";    public static final String TAG_ITEM_NAME = "item_name";    public static final String TAG_ITEM_APPROVE = "item_approve";    public static final String TAG_SUBCATEGORY_ID = "subcategory_id";    public static final String TAG_CHILD_CATEGORY = "child_category";    public static final String TAG_CHILD_CATEGORY_ID = "child_category_id";    public static final String TAG_CHILD_CATEGORY_NAME = "child_category_name";    public static final String TAG_TRACKID = "trackid";    public static final String TAG_CH_STATUS = "chstatus";    public static final String TAG_SENDER_ID = "sender_id";    public static final String TAG_SOURCE_ID = "source_id";    public static final String TAG_RECEIVER_ID = "receiver_id";    public static final String TAG_FACEBOOK_ID = "facebook_id";    public static final String TAG_FB_VER = "facebook_ver";    public static final String TAG_EMAIL_VER = "email_ver";    public static final String TAG_FACEBOOK = "facebook";    public static final String TAG_MOB_NO = "mob_no";    public static final String TAG_MOB_VER = "mob_ver";    public static final String TAG_CURRENT_LATITUDE = "current_latitude";    public static final String TAG_CURRENT_LONGITUDE = "current_longitude";    public static final String TAG_MERCHANT_ID = "merchant_id";    public static final String TAG_BANNER_URL = "bannerURL";    public static final String TAG_BANNER_IMAGE = "bannerImage";    public static final String TAG_PAGE_NAME = "page_name";    public static final String TAG_PAGE_CONTENT = "page_content";    public static final String TAG_LAT = "lat";    public static final String TAG_LON = "lon";    public static final String TAG_ITEM_PRICE = "item_price";    public static final String TAG_DISTANCE = "distance";    public static final String TAG_SORTING_ID = "sorting_id";    public static final String TAG_POSTED_WITHIN = "posted_within";    public static final String TAG_POSTED_TEXT = "posted_text";    public static final String TAG_SEARCH_KEY = "search_key";    public static final String TAG_SEARCH = "search";    public static final String TAG_SEARCH_TYPE = "search_type";    public static final String TAG_PASSWORD = "password";    public static final String TAG_EMAIL = "email";    public static final String TAG_ORDER_ID = "orderid";    public static final String TAG_SALEDATE = "saledate";    public static final String TAG_ORDERITEMS = "orderitems";    public static final String TAG_ORDERIMG = "orderImage";    public static final String TAG_ITEMNAME = "itemname";    public static final String TAG_UPRICE = "unitprice";    public static final String TAG_SIZE = "size";    public static final String TAG_SYMBOL = "symbol";    public static final String TAG_TOTAL = "total";    public static final String TAG_CLAIM = "claim";    public static final String TAG_CANCEL = "cancel";    public static final String TAG_SHIPPING = "shippingaddress";    public static final String TAG_COUNTRY = "country";    public static final String TAG_STATE = "state";    public static final String TAG_CITY = "city";    public static final String TAG_PHONE = "phone";    public static final String TAG_COUNTRYCODE = "countrycode";    public static final String TAG_ADDRESS1 = "address1";    public static final String TAG_ADDRESS2 = "address2";    public static final String TAG_ZIPCODE = "zipcode";    public static final String TAG_TRACKING_DETAILS = "trackingdetails";    public static final String TAG_TRACK_ID = "trackingid";    public static final String TAG_COURIER_NAME = "couriername";    public static final String TAG_COURIER_SERVICE = "courierservice";    public static final String TAG_NOTES = "notes";    public static final String TAG_SHIPPING_DATE = "shippingdate";    public static final String TAG_PAYMENT_TYPE = "payment_type";    public static final String TAG_DELIVERY_DATE = "deliverydate";    public static final String TAG_INVOICE = "invoice";    public static final String TAG_ITEM_DATA = "item_data";    public static final String TAG_SHIPPING_ID = "shipping_id";    public static final String TAG_HAS_CHANGES = "has_changes";    public static final String TAG_DEFAULTSHIPPING = "defaultshipping";    public static final String TAG_USER_ID = "userId";    public static final String TAG_USERIMAGE_M = "userimage";    public static final String TAG_ITEM_TITLE = "item_title";    public static final String TAG_TITLE_M = "title";    public static final String TAG_CHAT_COUNT = "chatCount";    public static final String TAG_NOTIFICATION_COUNT = "notificationCount";    public static final String TAG_REVIEW_ID = "review_id";    public static final String TAG_RATING = "rating";    public static final String TAG_REVIEW_TITLE = "review_title";    public static final String TAG_REVIEW_DES = "review_des";    public static final String TAG_CREATED_DATE = "created_date";    public static final String TAG_ORDERID = "order_id";    public static final String TAG_OFFSET = "offset";    public static final String TAG_LIMIT = "limit";    public static final String TAG_NONCE = "nonce";    public static final String TAG_NICK_NAME = "nick_name";    public static final String TAG_PHONE_NO = "phone_no";    public static final String TAG_ZIP_CODE = "zip_code";    public static final String TAG_DEFAULT = "default";    public static final String TAG_PROMOTION_ID = "promotion_id";    public static final String TAG_PAY_NONCE = "pay_nonce";    public static final String TAG_FOLLOW_ID = "follow_id";    public static final String TAG_CHAT_TYPE = "chat_type";    public static final String TAG_OTP = "otp";    public static final String TAG_VALUE = "value";    public static final String TAG_MYITEM_ID = "myitem_id";    public static final String TAG_EXCHANGE_ITEM_ID = "exchangeitem_id";    public static final String TAG_COMMENTS = "comments";    public static final String TAG_SOLD = "sold";    public static final String TAG_REMOVE_IMG = "remove_img";    public static final String TAG_ITEMDES = "item_des";    public static final String TAG_PRODUCT_IMG = "product_img";    public static final String TAG_ADDRESS = "address";    public static final String TAG_CURRENCY = "currency";    public static final String TAG_EXCHANGE_TO_BUY = "exchange_to_buy";    public static final String TAG_PRODUCT_CONDITION = "product_condition";    public static final String TAG_CONDITION_ID = "condition_id";    public static final String TAG_CONDITION_NAME = "condition_name";    public static final String TAG_PRODUCT_URL = "product_url";    public static final String TAG_ACTION_ID = "action_id";    public static final String TAG_ACTION_VALUE = "action_value";    public static final String TAG_BLOCK = "block";    public static final String TAG_BLOCKED_My_ME = "blocked_by_me";    public static final String TAG_BANNER = "banner";    public static final String TAG_BUYNOW = "buynow";    public static final String TAG_PROMOTION = "promotion";    public static final String TAG_DISTANCE_TYPE = "distance_type";    public static final String TAG_BANNER_DATA = "bannerData";    public static final String TAG_FIRST_NAME = "first_name";    public static final String TAG_LAST_NAME = "last_name";    public static final String TAG_IMAGE_URL = "image_url";    public static final String TAG_SHOW_MOBILE_NO = "show_mobile_no";    public static final String TAG_FB_EMAIL = "fb_email";    public static final String TAG_FB_FIRSTNAME = "fb_firstname";    public static final String TAG_FB_LASTNAME = "fb_lastname";    public static final String TAG_FB_PHONE = "fb_phone";    public static final String TAG_FB_PROFILEURL = "fb_profileurl";    public static final String TAG_PROFILE_URL = "profile_url";    public static final String TAG_OLD_PASSWORD = "old_password";    public static final String TAG_NEW_PASSWORD = "new_password";    public static final String TAG_DEVICE_TOKEN = "deviceToken";    public static final String TAG_DEVICE_MODE = "devicemode";    public static final String TAG_DEVICE_TYPE = "devicetype";    public static final String TAG_DEVICE_ID = "deviceId";    public static final String TAG_INSTANT_BUY = "instant_buy";    public static final String TAG_OFFER_CURRENCY = "offer_currency";    public static final String TAG_OFFER_CURRENCY_CODE = "offer_currency_code";    public static final String TAG_OFFER_TOTAL = "offer_total";    public static final String TAG_FORMATTED_OFFER_PRICE = "formatted_offer_price";    public static final String TAG_FORMATTED_OFFER_TOTAL = "formatted_offer_total";    public static final String TAG_FORMATTED_SHIPPING_PRICE = "formatted_shipping_price";    public static final String TAG_PATH = "path";    public static final String TAG_START_DATE = "start_date";    public static final String TAG_END_DATE = "end_date";    public static final String TAG_APP = "app";    public static final String TAG_APP_BANNER_NAME = "app_banner_name";    public static final String TAG_APP_BANNER_URL = "app_banner_url";    public static final String TAG_APP_BANNER_LINK = "app_banner_link";    public static final String TAG_WEB = "web";    public static final String TAG_WEB_BANNER_NAME = "web_banner_name";    public static final String TAG_WEB_BANNER_URL = "web_banner_url";    public static final String TAG_WEB_BANNER_LINK = "web_banner_link";    public static final String TAG_NO_DATES = "no_dates";    public static final String TAG_PRODUCT_ID = "product_id";    public static final String TAG_HIGH = "high";    public static final String TAG_MEDIUM = "medium";    public static final String TAG_LOW = "low";    public static final String TAG_RANGE = "range";    public static final String TAG_DROPDOWN = "dropdown";    public static final String TAG_MULTILEVEL = "multilevel";    public static final String TAG_FILTERS = "filters";    public static final String TAG_RANGE_DATA = "range_data";    public static final String TAG_DROPDOWN_DATA = "dropdown_data";    public static final String TAG_MULTILEVEL_DATA = "multilevel_data";    public static final String TAG_PARENT_ID = "parent_id";    public static final String TAG_SUBPARENT_ID = "subparent_id";    public static final String TAG_CHILD_ID = "child_id";    public static final String TAG_FILTER_ID = "filter_id";    public static final String TAG_PARENT_LABEL = "parent_label";    public static final String TAG_CHILD_LABEL = "child_label";    public static final String TAG_YOUTUBE_LINK = "youtube_link";    public static final String TAG_GALLERY = "gallery";    public static final String TAG_CAMERA = "camera";    public static final String TAG_ADD = "add";    public static final String TAG_EDIT = "edit";    public static final String TAG_MIN_VALUE = "min_value";    public static final String TAG_MAX_VALUE = "max_value";    public static final String TAG_CITY_NAME = "city_name";    public static final String TAG_STATE_NAME = "state_name";    public static final String TAG_COUNTRY_NAME = "country_name";    public static final String TAG_COUNTRYID = "country_id";    public static final String TAG_COUNTRY_CODE = "country_code";    public static final String TAG_PROFILE = "profile";    public static final String TAG_REMOVE_SHIPPING = "removeshipping";    public static final String TAG_PRICE_MAX = "priceMax";    public static final String TAG_PRICE_MIN = "priceMin";    public static final String TAG_ZERO_MAX = "zeroMax";    public static final String TAG_NOTIFICATION = "notification";    public static final String TAG_DEVICE_NAME = "device_name";    public static final String TAG_DEVICE_MODEL = "device_model";    public static final String TAG_DEVICE_OS = "device_os";    public static final String TAG_STRIPE = "stripe";    public static final String TAG_STRIPE_PRIVATEKEY = "stripe_privatekey";    public static final String TAG_STRIPE_PUBLICKEY = "stripe_publickey";    public static final String TAG_BRAINTREE = "braintree";    public static final String TAG_REQUEST_DATA = "request_data";    /**     * Static Keywords for Intent Keys     **/    public static final String SHIPPING_FEE = "shipping_fee";    public static final String ORDER_TOTAL = "order_total";    public static final String CONTENT = "content";    public static final String DATE = "date";    public static final String CHATID = "chatId";    public static final String TAG_LANG_TYPE = "lang_type";    public static final String TAG_NAME = "name";    public static final String CATEGORYID = "categoryId";    public static final String IMAGETYPE = "image_type";    public static final String TAG_FROM = "from";    public static final String KEY_URL = "url";    public static final String KEY_IMAGE = "image";    public static final String TAG_IMAGES = "images";    public static final String TAG_POSITION = "position";    public static final String TAG_DATA = "data";    public static final String FILTER_NAME = "filter_name";    public static final String TAG_HOME = "home";    public static final String TAG_DETAIL = "detail";    public static final String TAG_SHIPPING_DATA = "shipping_data";    public static final String TAG_CHECKOUT = "checkout";    public static final String TAG_START_CHAT = "start_chat";    /**     * Static Keywords for Preferences     **/    public static final String PREF_DISTANCE_TYPE = "distanceType";    public static final String PREF_LANGUAGE = "language";    public static final String PREF_LANGUAGE_CODE = "language_code";    public static final String PREF_BUYNOW = "buynow";    public static final String PREF_EXCHANGE = "exchange";    public static final String PREF_PROMOTION = "promotion";    public static final String PREF_ISLOGGED = "isLogged";    public static final String PREF_GIVINGAWAY = "isLogged";    public static final String PREF_PAIDBANNER = "paid_banner";    public static final String PREF_SITEMAINTAIN = "site_maintain";    /**     * Static Keywords for Sockets     **/    public static final String SOCK_RECEIVERID = "receiverId";    public static final String SOCK_RECEIVER = "receiver";    public static final String SOCK_SENDERID = "senderId";    public static final String SOCK_VIEW_URL = "view_url";    public static final String SOCK_MESSAGE_CONTENT = "messageContent";    public static final String SOCK_USERNAME = "userName";    public static final String SOCK_USERIMAGE = "userImage";    public static final String SOCK_SOURCE_ID = "sourceId";    public static final String SOCK_SITE_BUYNOW_PAYMENT_MODE = "site_buynowPaymentMode";    public static final String SOCK_SOLD_ITEM = "sold_item";    public static final String SOCK_BUYNOW_URL = "buynow_url";    /**     * Static Keywords for Permissions     **/    public static final String[] LOCATION_PERMISSIONS = new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};    public static final String[] CAMERA_PERMISSION = new String[]{CAMERA, WRITE_EXTERNAL_STORAGE};    public static final String[] STORAGE_PERMISSION = new String[]{WRITE_EXTERNAL_STORAGE};    public static final int CAMERA_PERMISSION_CODE = 100;    public static final int STORAGE_PERMISSION_CODE = 101;    public static final int LOCATION_PERMISSION_CODE = 102;    public static final int CATEGORY_REQUEST_CODE = 200;    public static final int SUB_CATEGORY_REQUEST_CODE = 201;    public static final int FILTER_REQUEST_CODE = 202;    public static final int PAYMENT_REQUEST_CODE = 300;    public static final int STRIPE_REQUEST_CODE = 301;    public static final int LOCATION_REQUEST_CODE = 500;    public static final int CONDITION_REQUEST_CODE = 600;    public static final int SEARCH_REQUEST_CODE = 700;    public static final int COMMENTS_REQUEST_CODE = 800;    public static final int PROFILE_REQUEST_CODE = 900;    public static final int ADDRESS_REQUEST_CODE = 901;    public static final int ADDRESS_EDIT_REQUEST_CODE = 902;    public static final int ADDRESS_REQ_CODE = 903;    public static final int ACTION_EDIT_REQUEST_CODE = 400;    public static final int ADD_IMAGES_REQUEST_CODE = 401;    public static final String UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";    public static final String UI_DATE_PATTERN = "yyyy-MM-dd";    public static final String TAG_CODE = "code";    public static final String TAG_PREFIX = "prefix";}