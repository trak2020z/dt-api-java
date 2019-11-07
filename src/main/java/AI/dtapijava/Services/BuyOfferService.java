package AI.dtapijava.Services;


import AI.dtapijava.Components.ExecDetailsHelper;
import AI.dtapijava.DTOs.Request.AddBuyOfferReqDTO;
import AI.dtapijava.DTOs.Response.ExecDetailsResDTO;
import AI.dtapijava.DTOs.Response.ExecTimeResDTO;
import AI.dtapijava.Entities.BuyOffer;
import AI.dtapijava.Entities.Company;
import AI.dtapijava.Entities.Resource;
import AI.dtapijava.Entities.User;
import AI.dtapijava.Exceptions.UserNotFoundExceptions;
import AI.dtapijava.Infrastructure.Util.UserUtils;
import AI.dtapijava.Repositories.BuyOfferRepository;
import AI.dtapijava.Repositories.CompanyRepository;
import AI.dtapijava.Repositories.ResourceRepository;
import AI.dtapijava.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BuyOfferService {
    @Autowired
    private BuyOfferRepository buyOfferRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResourceRepository resourceRepository;

    public BuyOffer getBuyOffer (int id) {
        return buyOfferRepository.getOne(id);
    }

    public List<BuyOffer> getBuyOffers() {
        return buyOfferRepository.findAll();
    }

    public List<BuyOffer> getBuyOffersValid (Boolean valid) {
        return buyOfferRepository.findByIsValid(valid);
    }

    public ExecTimeResDTO addBuyOffer(AddBuyOfferReqDTO addBuyOfferReqDTO) {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();

        execHelper.setStartDbTime(OffsetDateTime.now());
        Company company = companyRepository.findById(addBuyOfferReqDTO.getCompanyId()).orElseThrow(() -> new RuntimeException("Company not found"));
        User user = userRepository.findById(UserUtils.getCurrentUserId())
                .orElseThrow(()->new UserNotFoundExceptions("User not found!"));
        Optional<Resource> resource = resourceRepository.findByUserAndCompany(user, company);
        execHelper.addNewDbTime();
        if (resource.isEmpty()) {
            resource = Optional.of(Resource.builder()
                    .user(user)
                    .company(company)
                    .amount(0)
                    .build());
            /*
            depracted - resource is saved with persist cascade
            execHelper.setStartDbTime(OffsetDateTime.now());
            resourceRepository.save(resource.get());
            execHelper.addNewDbTime();
             */
        }

        BuyOffer buyOffer = BuyOffer.builder()
                .resource(resource.get())
                .isValid(true)
                .amount(addBuyOfferReqDTO.getAmount())
                .startAmount(addBuyOfferReqDTO.getAmount())
                .date(OffsetDateTime.now())
                .build();
        execHelper.setStartDbTime(OffsetDateTime.now());
        buyOfferRepository.save(buyOffer);
        execHelper.addNewDbTime();

        return new ExecTimeResDTO(new ExecDetailsResDTO(execHelper.getDbTime(),execHelper.getExecTime()));
    }

    public ExecTimeResDTO withdrawBuyOffer(int id) {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();

        execHelper.setStartDbTime(OffsetDateTime.now());
        BuyOffer buyOffer = buyOfferRepository.findById(id).orElseThrow(() -> new RuntimeException("Buy offer not found"));
        execHelper.addNewDbTime();
        buyOffer.setIsValid(false);
        execHelper.setStartDbTime(OffsetDateTime.now());
        buyOfferRepository.save(buyOffer);
        execHelper.addNewDbTime();

        return new ExecTimeResDTO(new ExecDetailsResDTO(execHelper.getDbTime(),execHelper.getExecTime()));
    }
}
