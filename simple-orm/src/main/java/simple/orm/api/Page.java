package simple.orm.api;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {
    public static final int DEFAULT_PAGE_NO;
    public static final int DEFAULT_PAGE_SIZE;
    private static final long serialVersionUID = -5096317825486579427L;

    static {
        Resource res = new ClassPathResource("simple.orm.api.properties");
        Configuration conf = null;
        try {
            conf = new PropertiesConfiguration(res.getURL());
        } catch (ConfigurationException | IOException e) {
            e.printStackTrace();
        }
        DEFAULT_PAGE_NO = conf.getInt("default.orm.page.start");
        DEFAULT_PAGE_SIZE = conf.getInt("default.orm.page.size");
    }

    private int pageNo;
    private int pageSize;
    private List<T> result;
    private long totalSize;

    public Page(List<T> result, long totalSize, int pageNo, int pageSize) {
        this.setPageNo(pageNo);
        this.setPageSize(pageSize);
        this.setResult(result);
        this.setTotalSize(totalSize);
    }

    public static final <T> Page<T> build(List<T> result, Class<T> clazz, int pageNo, int pageSize, long totalSize) {
        return new Page<>(result, totalSize, pageNo, pageSize);
    }

    public static final <T> Page<T> build(List<T> result, int pageNo, int pageSize, long totalSize) {
        return new Page<>(result, totalSize, pageNo, pageSize);
    }

    public static final <T> Page<T> build(List<T> result, Class<T> clazz, long totalSize) {
        return new Page<>(result, totalSize, DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
    }

    public static final <T> Page<T> build(List<T> result, Class<T> clazz, long totalSize, int pageNo) {
        return new Page<>(result, totalSize, pageNo, DEFAULT_PAGE_SIZE);
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = Math.max(DEFAULT_PAGE_NO, pageNo);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = Math.max(0L, totalSize);
    }

    public int getTotalPage() {
        return Long.valueOf(this.totalSize % this.pageSize == 0 ? this.totalSize / this.pageSize
                : this.totalSize / this.pageSize + 1).intValue();
    }

    public int getStartIndex() {
        return (this.pageNo - 1) * this.pageSize;
    }

    public int getEndIndex() {
        int foo = this.getStartIndex();
        if (CollectionUtils.isNotEmpty(this.result)) {
            foo += this.result.size();
        }
        return foo;
    }
}