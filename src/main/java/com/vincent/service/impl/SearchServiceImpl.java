package com.vincent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vincent.entity.Post;
import com.vincent.search.model.PostDocment;
import com.vincent.search.mq.PostMqIndexMessage;
import com.vincent.search.repository.PostRepository;
import com.vincent.service.PostService;
import com.vincent.service.SearchService;
import com.vincent.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    PostRepository postRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PostService postService;

    @Override
    public IPage search(Page page, String keyword) {

        // 分页信息 mybatis plus 的page转成jpa的page
        Long current = page.getCurrent() - 1;
        Long size = page.getSize();

        Pageable pageable = PageRequest.of(current.intValue(), size.intValue());

        // 搜索es得到pageData
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyword, "title", "authorName", "categoryName");
        org.springframework.data.domain.Page<PostDocment> docments = postRepository.search(multiMatchQueryBuilder, pageable);

        // 结果信息jpa的pageData转成mybatis plus的pageData
        IPage pageData = new Page(page.getCurrent(), page.getSize(), docments.getTotalElements());
        pageData.setRecords(docments.getContent());
        return pageData;

    }

    @Override
    public int initEsData(List<PostVo> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }

        List<PostDocment> documents = new ArrayList<>();
        for (PostVo vo : records) {
            // 映射转换
            PostDocment postDocment = modelMapper.map(vo, PostDocment.class);
            documents.add(postDocment);
        }
        postRepository.saveAll(documents);

        return documents.size();
    }

    @Override
    public void createOrUpdateIndex(PostMqIndexMessage message) {
        Long postId = message.getPostId();
        PostVo postVo = postService.selectOnePost(new QueryWrapper<Post>().eq("p.id", postId));
        PostDocment postDocment = modelMapper.map(postVo, PostDocment.class);
        postRepository.save(postDocment);
        log.info("es 索引更新成功！ --> {}", postDocment.toString());
    }

    @Override
    public void removeIndex(PostMqIndexMessage message) {
        Long postId = message.getPostId();
        postRepository.deleteById(postId);
        log.info("es 索引删除成功！ --> {}", message.toString());
    }
}
