// Java
package com.example.f1widget2.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 600L;

    public static F1ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request requestWithUa = original.newBuilder()
                                .header("User-Agent", "F1Widget/1.0 (openf1; android)")
                                .build();

                        Response response = null;
                        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
                            if (response != null) {
                                response.close();
                            }

                            response = chain.proceed(requestWithUa);
                            int code = response.code();
                            boolean retryable = code == 429 || (code >= 500 && code <= 599);

                            if (!retryable || attempt == MAX_RETRIES) {
                                return response;
                            }

                            long sleepMs = BASE_BACKOFF_MS * (1L << attempt);
                            String retryAfter = response.header("Retry-After");
                            if (retryAfter != null) {
                                try {
                                    sleepMs = Math.max(sleepMs, Long.parseLong(retryAfter) * 1000L);
                                } catch (NumberFormatException ignored) {
                                    // Ignore malformed Retry-After and keep exponential backoff.
                                }
                            }

                            try {
                                Thread.sleep(sleepMs);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return response;
                            }
                        }
                        return response;
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.openf1.org/v1/") // <\- bien terminer par '/'
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(F1ApiService.class);
    }
}
