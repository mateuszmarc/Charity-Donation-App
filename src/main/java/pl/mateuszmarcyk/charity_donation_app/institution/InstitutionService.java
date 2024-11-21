package pl.mateuszmarcyk.charity_donation_app.institution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public List<Institution> findAll() {
        return institutionRepository.findAll();
    }

    public List<List<Institution>> findInstitutionsGroupByTwo() {

        List<List<Institution>> institutionsGrouped = new ArrayList<>();
        List<Institution> institutions = institutionRepository.findAll();

        for (int i = 0; i < institutions.size(); i+=2) {
            institutionsGrouped.add(institutions.subList(i, Math.min(i + 2, institutions.size())));
        }
        return institutionsGrouped;
    }
}
