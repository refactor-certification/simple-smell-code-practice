package refactoring.kata.kanban.service;

import refactoring.kata.kanban.dto.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  <a href="mailto:meixuesong@gmail.com">Mei Xuesong</a>
 */
public class QueryService {
    public <E> List queryDocs(String index, QueryBuilder query, int i, int i1, Class<E> entityClass) {
        return new ArrayList();
    }
}