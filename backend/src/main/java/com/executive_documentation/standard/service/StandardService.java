package com.executive_documentation.standard.service;


import com.executive_documentation.standard.model.Standard;

import java.util.List;

public interface StandardService {
    Standard get(Long id);

    List<Standard> getAll();

    Standard create(Standard standard);

    Standard update(long id, Standard standard);

    void delete(long id);

    Standard findStandardOrNot(long id);
}
