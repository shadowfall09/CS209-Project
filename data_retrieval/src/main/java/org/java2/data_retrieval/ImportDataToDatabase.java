package org.java2.data_retrieval;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.java2.data_retrieval.entity.*;
import org.java2.data_retrieval.mapper.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ImportDataToDatabase {
    public static void main(String[] args) {
        // Configure HikariCP
        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl("jdbc:mysql://47.107.113.54:3306/stackoverflow");
//        hikariConfig.setUsername("root");
//        hikariConfig.setPassword("soawjd47628");
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/stackoverflow");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");

        // Create a DataSource
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        // Create a TransactionFactory
        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();

        // Create an Environment
        Environment environment = new Environment("development", transactionFactory, dataSource);

        // Create a Configuration
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        configuration.addMappers("org.java2.data_retrieval.mapper");

        // Create a SqlSessionFactory
        MybatisSqlSessionFactoryBuilder builder = new MybatisSqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(configuration);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()){
            AnswerMapper answerMapper = sqlSession.getMapper(AnswerMapper.class);
            AnswerTagRelationMapper answerTagRelationMapper = sqlSession.getMapper(AnswerTagRelationMapper.class);
            CommentMapper commentMapper = sqlSession.getMapper(CommentMapper.class);
            OwnerMapper ownerMapper = sqlSession.getMapper(OwnerMapper.class);
            QuestionMapper questionMapper = sqlSession.getMapper(QuestionMapper.class);
            QuestionTagRelationMapper questionTagRelationMapper = sqlSession.getMapper(QuestionTagRelationMapper.class);
            TagMapper tagMapper = sqlSession.getMapper(TagMapper.class);
            int fileId = 1;
            Path filePath = Paths.get("data_retrieval/Data/originFile" + fileId + ".json");
            while (Files.exists(filePath)) {
                System.out.println(fileId);
                String content = Files.readString(filePath);
                JSONObject jsonObject = JSONObject.parseObject(content);
                JSONArray itemArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < itemArray.size(); i++) {
                    System.out.println(fileId + "." + i);
                    JSONObject item = itemArray.getJSONObject(i);
                    JSONArray questionTagArray = item.getJSONArray("tags");
                    List<Integer> questionTagIdList = addTags(questionTagArray, tagMapper);
                    JSONObject questionOwner = item.getJSONObject("owner");
                    addOwner(questionOwner, ownerMapper);
                    Question question = new Question();
                    question.setId(item.getString("question_id"));
                    question.setOwnerId(questionOwner.getString("user_id"));
                    question.setViewCount(item.getInteger("view_count"));
                    question.setFavoriteCount(item.getInteger("favorite_count"));
                    question.setUpVote(item.getInteger("up_vote_count"));
                    question.setDownVote(item.getInteger("down_vote_count"));
                    Long closeDataLong = item.getLong("closed_date");
                    if (closeDataLong != null) {
                        question.setClosedDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(closeDataLong), ZoneId.systemDefault()));
                    }
                    question.setScore(item.getInteger("score"));
                    question.setLastActiveDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(item.getLong("last_activity_date")), ZoneId.systemDefault()));
                    question.setCreationDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(item.getLong("creation_date")), ZoneId.systemDefault()));
                    question.setTitle(item.getString("title"));
                    question.setContent(item.getString("body"));
                    try {
                        questionMapper.insert(question);
                    }
                    catch (PersistenceException e) {
                        if (!(e.getCause() instanceof SQLIntegrityConstraintViolationException)) {
                            throw new RuntimeException(e);
                        }
                        else {
                            continue;
                        }
                    }
                    for (Integer questionTagId : questionTagIdList) {
                        QuestionTagRelation questionTagRelation = new QuestionTagRelation();
                        questionTagRelation.setQuestionId(question.getId());
                        questionTagRelation.setTagId(questionTagId);
                        questionTagRelationMapper.insert(questionTagRelation);
                    }
                    addComments(commentMapper, ownerMapper, item);
                    JSONArray questionAnswerArray = item.getJSONArray("answers");
                    if (questionAnswerArray != null) {
                        for (int j = 0; j < questionAnswerArray.size(); j++) {
                            JSONObject questionAnswer = questionAnswerArray.getJSONObject(j);
                            JSONArray answerTagArray = questionAnswer.getJSONArray("tags");
                            List<Integer> answerTagIdList = addTags(answerTagArray, tagMapper);
                            JSONObject answerOwner = questionAnswer.getJSONObject("owner");
                            addOwner(answerOwner, ownerMapper);
                            Answer answer = new Answer();
                            answer.setId(questionAnswer.getString("answer_id"));
                            answer.setOwnerId(answerOwner.getString("user_id"));
                            answer.setLastActiveDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(questionAnswer.getLong("last_activity_date")), ZoneId.systemDefault()));
                            answer.setDownVote(questionAnswer.getInteger("down_vote_count"));
                            answer.setUpVote(questionAnswer.getInteger("up_vote_count"));
                            answer.setContent(questionAnswer.getString("body"));
                            answer.setScore(questionAnswer.getInteger("score"));
                            answer.setTitle(questionAnswer.getString("title"));
                            answer.setIsAccepted(questionAnswer.getBoolean("is_accepted"));
                            answer.setQuestionId(questionAnswer.getString("question_id"));
                            answerMapper.insert(answer);
                            for (Integer answerTagId : answerTagIdList) {
                                AnswerTagRelation answerTagRelation = new AnswerTagRelation();
                                answerTagRelation.setAnswerId(answer.getId());
                                answerTagRelation.setTagId(answerTagId);
                                answerTagRelationMapper.insert(answerTagRelation);
                            }
                            addComments(commentMapper, ownerMapper, questionAnswer);
                        }
                    }
                }
                sqlSession.commit();
                fileId++;
                filePath = Paths.get("data_retrieval/Data/originFile" + fileId + ".json");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addComments(CommentMapper commentMapper, OwnerMapper ownerMapper, JSONObject parent) {
        JSONArray questionCommentArray = parent.getJSONArray("comments");
        if (questionCommentArray != null) {
            for (int j = 0; j < questionCommentArray.size(); j++) {
                JSONObject questionComment = questionCommentArray.getJSONObject(j);
                JSONObject commentOwner = questionComment.getJSONObject("owner");
                addOwner(commentOwner, ownerMapper);
                Comment comment = new Comment();
                comment.setId(questionComment.getString("comment_id"));
                comment.setOwnerId(commentOwner.getString("user_id"));
                comment.setContent(questionComment.getString("body"));
                comment.setScore(questionComment.getInteger("score"));
                comment.setCreationDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(questionComment.getLong("creation_date")), ZoneId.systemDefault()));
                comment.setPostId(questionComment.getString("post_id"));
                commentMapper.insert(comment);
            }
        }
    }

    public static void addOwner(JSONObject ownerInfo, OwnerMapper ownerMapper) {
        Owner owner = new Owner();
        owner.setId(ownerInfo.getString("user_id"));
        owner.setName(ownerInfo.getString("display_name"));
        owner.setReputation(ownerInfo.getInteger("reputation"));
        try {
            ownerMapper.insert(owner);
        }
        catch (PersistenceException e) {
            if (!(e.getCause() instanceof SQLIntegrityConstraintViolationException)) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<Integer> addTags(JSONArray tagArray, TagMapper tagMapper) {
        List<Integer> questionTagIdList = new ArrayList<>();
        for (int j = 0; j < tagArray.size(); j++) {
            String tagName = tagArray.getString(j);
            Tag tag = new Tag();
            tag.setTagName(tagName);
            try {
                tagMapper.insert(tag);
                questionTagIdList.add(tag.getId());
            }
            catch (PersistenceException e) {
                if (!(e.getCause() instanceof SQLIntegrityConstraintViolationException)) {
                    throw new RuntimeException(e);
                }
                else {
                    Tag dbTag = tagMapper.selectOne(new QueryWrapper<Tag>().eq("tag_name", tagName));
                    questionTagIdList.add(dbTag.getId());
                }
            }
        }
        return questionTagIdList;
    }
}
