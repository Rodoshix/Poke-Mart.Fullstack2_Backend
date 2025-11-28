package cl.pokemart.pokemart_backend.dto.common;

/**
 * Ejemplos reutilizables de respuestas de error para Swagger.
 */
public final class ApiErrorExamples {
    private ApiErrorExamples() {}

    public static final String BLOG_BAD_REQUEST = """
            {
              "status": 400,
              "error": "Bad Request",
              "message": "Categoria invalida",
              "path": "/api/v1/admin/blogs",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String BLOG_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Blog no encontrado",
              "path": "/api/v1/admin/blogs/99",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String OFFER_BAD_REQUEST = """
            {
              "status": 400,
              "error": "Bad Request",
              "message": "Descuento invalido (1-99%)",
              "path": "/api/v1/admin/offers",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String OFFER_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Oferta no encontrada",
              "path": "/api/v1/admin/offers/77",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String OFFER_FORBIDDEN = """
            {
              "status": 403,
              "error": "Forbidden",
              "message": "Sin permisos para administrar ofertas",
              "path": "/api/v1/admin/offers",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String PUBLIC_OFFERS_ERROR = """
            {
              "status": 500,
              "error": "Internal Server Error",
              "message": "No se pudieron obtener las ofertas",
              "path": "/api/v1/offers",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String UPLOAD_TOO_LARGE = """
            {
              "status": 413,
              "error": "Payload Too Large",
              "message": "El archivo supera el limite permitido",
              "path": "/api/v1/uploads/images",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String UPLOAD_UNSUPPORTED = """
            {
              "status": 415,
              "error": "Unsupported Media Type",
              "message": "Solo se aceptan imagenes JPG, PNG o WEBP",
              "path": "/api/v1/uploads/images",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String ORDER_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Orden no encontrada",
              "path": "/api/v1/admin/orders/123",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String ORDER_BAD_REQUEST = """
            {
              "status": 400,
              "error": "Bad Request",
              "message": "Estado invalido",
              "path": "/api/v1/admin/orders/123",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String AUTH_BAD_CREDENTIALS = """
            {
              "status": 401,
              "error": "Unauthorized",
              "message": "Credenciales invalidas",
              "path": "/api/v1/auth/login",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String AUTH_CONFLICT = """
            {
              "status": 409,
              "error": "Conflict",
              "message": "El correo ya esta registrado",
              "path": "/api/v1/auth/register",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String USER_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Usuario no encontrado",
              "path": "/api/v1/admin/users/99",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String USER_CONFLICT = """
            {
              "status": 409,
              "error": "Conflict",
              "message": "El email ya esta registrado",
              "path": "/api/v1/admin/users",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String BLOG_PUBLIC_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Entrada de blog no encontrada",
              "path": "/api/v1/blogs/mi-slug",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String BLOG_PUBLIC_ERROR = """
            {
              "status": 500,
              "error": "Internal Server Error",
              "message": "No se pudo cargar el blog",
              "path": "/api/v1/blogs/mi-slug",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String PROFILE_UNAUTHORIZED = """
            {
              "status": 401,
              "error": "Unauthorized",
              "message": "No autenticado",
              "path": "/api/v1/profile",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String ORDER_PUBLIC_BAD_REQUEST = """
            {
              "status": 400,
              "error": "Bad Request",
              "message": "Carrito vacio o datos incompletos",
              "path": "/api/v1/orders",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String ORDER_PUBLIC_UNAUTHORIZED = """
            {
              "status": 401,
              "error": "Unauthorized",
              "message": "Debes iniciar sesion para crear ordenes",
              "path": "/api/v1/orders",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String PAYMENT_BAD_REQUEST = """
            {
              "status": 400,
              "error": "Bad Request",
              "message": "Preferencia de pago invalida",
              "path": "/api/v1/payments/mp/preference",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String PAYMENT_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Pago no encontrado o expirado",
              "path": "/api/v1/payments/mp/confirm",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String PAYMENT_CONFLICT = """
            {
              "status": 409,
              "error": "Conflict",
              "message": "El pago ya fue procesado",
              "path": "/api/v1/payments/mp/confirm",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String REVIEW_FORBIDDEN = """
            {
              "status": 403,
              "error": "Forbidden",
              "message": "Debes iniciar sesion para reseñar",
              "path": "/api/v1/products/1/reviews",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;

    public static final String REVIEW_NOT_FOUND = """
            {
              "status": 404,
              "error": "Not Found",
              "message": "Producto no encontrado",
              "path": "/api/v1/products/999/reviews",
              "timestamp": "2025-11-27T10:15:30Z"
            }
            """;
}
