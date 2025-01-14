package pl.mateuszmarcyk.charity_donation_app;

public class UrlTemplates {
    public static final String ACCESS_DENIED_URL = "/error/403";
    public static final String ERROR_URL = "/error";
    public static final String APPLICATION_URL = "/app";
    public static final String APP_ADMIN_DASHBOARD_URL = "/app/admins/dashboard";
    public static final String ADMIN_DASHBOARD_URL = "/admins/dashboard";
    public static final String ADMIN_ALL_ADMINS_URL = "/admins/all-admins";
    public static final String ADMIN_ALL_USERS_URL = "/admins/users";
    public static final String ADMIN_USER_ACCOUNT_DETAILS_URL = "/admins/users/{id}";
    public static final String ADMIN_USER_PROFILE_DETAILS_URL = "/admins/users/profiles/{id}";
    public static final String ADMIN_USER_PROFILE_DETAILS_EDIT_URL = "/admins/users/profiles/edit/{id}";
    public static final String ADMIN_USER_PROFILE_DETAILS_EDIT_POST_URL = "/admins/users/profiles/edit";
    public static final String ADMIN_USERS_PROFILES_URL = "/admins/users/profiles";
    public static final String ADMIN_USERS_ACCOUNT_EDIT_FORM_URL = "/admins/users/edit/{id}";
    public static final String ADMIN_USERS_EMAIL_CHANGE_URL = "/admins/users/change-email";
    public static final String ADMIN_USERS_PASSWORD_CHANGE_URL = "/admins/users/change-password";
    public static final String ADMIN_USERS_BLOCK_URL = "/admins/users/block/{id}";
    public static final String ADMIN_USERS_UNBLOCK_URL = "/admins/users/unblock/{id}";
    public static final String ADMIN_USERS_UPGRADE_URL = "/admins/users/upgrade/{id}";
    public static final String ADMIN_USERS_DOWNGRADE_URL = "/admins/users/downgrade/{id}";
    public static final String ADMIN_USERS_DELETE_URL = "/admins/users/delete";
    public static final String ADMIN_DONATIONS_URL = "/admins/donations";
    public static final String ADMIN_DONATIONS_ARCHIVE_URL = "/admins/donations/archive";
    public static final String ADMIN_DONATIONS_UN_ARCHIVE_URL = "/admins/donations/unarchive";
    public static final String ADMIN_DONATIONS_DELETE_URL = "/admins/donations/delete";
    public static final String ADMIN_DONATIONS_DONATION_DETAILS_URL = "/admins/donations/{id}";
    public static final String ADMIN_CATEGORIES_URL = "/admins/categories";
    public static final String ADMIN_CATEGORIES_DETAILS_URL = "/admins/categories/{categoryId}";
    public static final String ADMIN_CATEGORIES_ADD_URL = "/admins/categories/add";
    public static final String ADMIN_CATEGORIES_EDIT_URL = "/admins/categories/edit/{id}";
    public static final String ADMIN_CATEGORIES_DELETE_URL = "/admins/categories/delete";
    public static final String ADMIN_INSTITUTIONS_URL = "/admins/institutions";
    public static final String ADMIN_INSTITUTIONS_DETAILS_URL = "/admins/institutions/{id}";
    public static final String ADMIN_INSTITUTIONS_ADD_URL = "/admins/institutions/add";
    public static final String ADMIN_INSTITUTIONS_EDIT_URL = "/admins/institutions/edit/{id}";
    public static final String ADMIN_INSTITUTIONS_DELETE_URL = "/admins/institutions/delete";

    public static final String USER_DONATION_FORM_URL = "/donate";
    public static final String HOME_URL = "/";
    public static final String MESSAGE_URL = "/message";


    public static final String LOGIN_URL = "/login";
    public static final String LOGOUT_URL = "/logout";

    public static final String RESET_PASSWORD_URL = "/reset-password";
    public static final String RESET_PASSWORD_VERIFY_EMAIL_URL = "/reset-password/verifyEmail";
    public static final String NEW_PASSWORD_URL = "/new-password";

    public static final String REGISTRATION_URL = "/register";
    public static final String REGISTRATION_VERIFY_EMAIL_URL = "/register/verifyEmail";
    public static final String REGISTRATION_RESEND_TOKEN_URL = "/register/resendToken";


}
