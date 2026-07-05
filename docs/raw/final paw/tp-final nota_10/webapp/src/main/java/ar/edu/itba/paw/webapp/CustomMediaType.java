package ar.edu.itba.paw.webapp;

public final class CustomMediaType {

    private CustomMediaType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // USERS
    public static final String APPLICATION_USER_JSON = "application/vnd.grupi.user.v1+json";
    public static final String APPLICATION_USER_REVIEWER_JSON = "application/vnd.grupi.user.reviewer.v1+json";

    // PRODUCTS
    public static final String APPLICATION_PRODUCT_JSON = "application/vnd.grupi.product.v1+json";

    // COMPANIES
    public static final String APPLICATION_COMPANY_JSON = "application/vnd.grupi.company.v1+json";

    // POOLS
    public static final String APPLICATION_POOL_JSON = "application/vnd.grupi.pool.v1+json";

    // REQUESTS
    public static final String APPLICATION_REQUEST_JSON = "application/vnd.grupi.request.v1+json";

    // CATEGORIES
    public static final String APPLICATION_CATEGORY_JSON = "application/vnd.grupi.category.v1+json";

    // LOCATIONS
    public static final String APPLICATION_LOCATION_JSON = "application/vnd.grupi.location.v1+json";

    // REVIEWS
    public static final String APPLICATION_REVIEW_JSON = "application/vnd.grupi.review.v1+json";

    // REPORTS
    public static final String APPLICATION_REPORT_JSON = "application/vnd.grupi.report.v1+json";

}
