package ar.edu.itba.paw.models.paginator;

import java.util.ArrayList;
import java.util.List;

public class Paginator<T> {

    public static final int DEFAULT_PAGE_SIZE = 12;

    private final List<T> list;
    private final Integer page, pageSize, lastPage, totalItems;

    public Paginator() {
        this.list = new ArrayList<>();
        this.page = 0;
        this.pageSize = 0;
        this.lastPage = 0;
        this.totalItems = 0;
    }

    public Paginator(final List<T> list, final Integer page, final Integer pageSize, final Integer totalItems) {
        this.list = list;
        this.page = page;
        this.pageSize = pageSize;
        this.lastPage = (int) Math.ceil((double) totalItems / pageSize) - 1;
        this.totalItems = totalItems;
    }

    public List<T> getList() {
        return list;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getLastPage() {
        return lastPage;
    }

    public int getTotalItems() {
        return totalItems;
    }

}
