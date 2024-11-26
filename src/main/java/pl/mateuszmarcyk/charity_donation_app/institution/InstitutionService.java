package pl.mateuszmarcyk.charity_donation_app.institution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public List<Institution> findAll() {
        return institutionRepository.findAll();
    }

    public Institution findById(Long id) {

        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instytucja nie znaleziona", "Nie znaleziono instytucji z takim id"));
    }
}

