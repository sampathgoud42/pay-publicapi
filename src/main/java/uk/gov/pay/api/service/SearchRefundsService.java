package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRefundsRequestException;
import uk.gov.pay.api.model.RefundError;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.search.card.SearchRefunds;
import uk.gov.pay.api.validation.SearchRefundsValidator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.pay.api.model.RefundError.Code.SEARCH_REFUNDS_DIRECT_DEBIT_ERROR;

public class SearchRefundsService {
    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private static final String DEFAULT_PAGE = "1";
    private static final String DEFAULT_DISPLAY_SIZE = "500";
    private final ConnectorUriGenerator uriGenerator;
    private final Client client;
    private final ObjectMapper objectMapper;
    private final PublicApiConfig configuration;

    @Inject
    public SearchRefundsService(Client client,
                                PublicApiConfig configuration,
                                ConnectorUriGenerator uriGenerator,
                                ObjectMapper objectMapper) {

        this.client = client;
        this.configuration = configuration;
        this.uriGenerator = uriGenerator;
        this.objectMapper = objectMapper;
    }
    
    public Response getAllRefunds(Account account, RefundsParams params) {
        SearchRefundsValidator.validateSearchParameters(params);
        Map<String, String> queryParams = buildDefaultParams(params);
        SearchRefunds refundsService = new SearchRefunds(
                client,
                configuration,
                uriGenerator,
                objectMapper);

        if (account.getPaymentType().equals(TokenPaymentType.DIRECT_DEBIT)) {
            throw new BadRefundsRequestException(RefundError.aRefundError(SEARCH_REFUNDS_DIRECT_DEBIT_ERROR));
        }

        return refundsService.getSearchResponse(account, queryParams);
    }

    private Map<String, String> buildDefaultParams(RefundsParams params) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        
        queryParams.put(PAGE, Optional.ofNullable(params.getPage()).orElse(DEFAULT_PAGE));
        queryParams.put(DISPLAY_SIZE, Optional.ofNullable(params.getDisplaySize()).orElse(DEFAULT_DISPLAY_SIZE));
        return queryParams;
    }
}
