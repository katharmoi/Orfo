package com.kadirkertis.domain.interactor.qr;

import com.kadirkertis.domain.model.Item;
import com.kadirkertis.domain.repository.PlaceRepository;
import com.kadirkertis.domain.repository.ProductsRepository;
import com.kadirkertis.domain.services.QRCodeService;
import com.kadirkertis.domain.services.UserTrackingService;

import java.util.List;

import io.reactivex.Maybe;

/**
 * Created by Kadir Kertis on 11/29/2017.
 */

public class ParseQrCodeUseCase {

    private UserTrackingService userTrackingService;
    private QRCodeService<Object, String[]> qrService;
    private PlaceRepository placeRepository;
    private ProductsRepository productsRepository;

    public ParseQrCodeUseCase(UserTrackingService userTrackingService,
                              QRCodeService<Object, String[]> qrService,
                              PlaceRepository placeRepository,
                              ProductsRepository productsRepository) {
        this.userTrackingService = userTrackingService;
        this.qrService = qrService;
        this.placeRepository = placeRepository;
        this.productsRepository = productsRepository;
    }

    public Maybe<List<Item>> execute(Object codeResult) {
        return qrService.parseCode(codeResult)
                .flatMapMaybe(resultArray -> placeRepository.getPlace(resultArray[1]))
                .flatMap(place -> {
                    return userTrackingService.isUserIn(place.getLatitude(), place.getLongitude())
                            .flatMapMaybe(isIn -> {
                                if (isIn) return productsRepository.getProducts(place.getId());
                                else return Maybe.error(new UserNotAtPlaceException("User is not in place"));
                            });
                });

    }
}