package com.vincent.entity;

import com.vincent.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户中心
 * </p>
 *
 * @author Vincent
 * @since 2021-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserAction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 动作类型
     */
    private String action;

    /**
     * 得分
     */
    private Integer point;

    /**
     * 关联的帖子ID
     */
    private String postId;

    /**
     * 关联的评论ID
     */
    private String commentId;


}
