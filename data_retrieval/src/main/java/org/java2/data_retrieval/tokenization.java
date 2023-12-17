package org.java2.data_retrieval;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.java2.data_retrieval.entity.Answer;
import org.java2.data_retrieval.entity.Comment;
import org.java2.data_retrieval.entity.Question;
import org.java2.data_retrieval.mapper.AnswerMapper;
import org.java2.data_retrieval.mapper.CommentMapper;
import org.java2.data_retrieval.mapper.QuestionMapper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.*;

public class tokenization {

    public final static Set<String> stopwords = new HashSet<>(Arrays.asList(
            "in","a", "an", "the", "is", "at", "which", "on","over","and", "but", "if", "or", "because", "as", "until", "while"));
    public final static Set<String> punctuations = new HashSet<>(Arrays.asList(
            ",", ".", "?", "!", ":", ";", "\"", "'", "(", ")", "[", "]", "{", "}", "<", ">", "/", "\\", "|", "-", "_", "=", "+", "*", "&", "^", "%", "$", "#", "@", "`", "~"));
    public static StanfordCoreNLP pipeline;
    public static void main(String[] args) {
        // Configure HikariCP
        HikariConfig hikariConfig = new HikariConfig();
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

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnswerMapper answerMapper = sqlSession.getMapper(AnswerMapper.class);
            CommentMapper commentMapper = sqlSession.getMapper(CommentMapper.class);
            QuestionMapper questionMapper = sqlSession.getMapper(QuestionMapper.class);
            // 设置NLP的属性，只使用分词（tokenize）和句子分割（ssplit）
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit");
            pipeline = new StanfordCoreNLP(props);
            List<Answer> answerList = answerMapper.selectList(null);
            for (Answer answer : answerList) {
                handleAnswer(answer, answerMapper);
            }
            sqlSession.commit();
            List<Comment> commentList = commentMapper.selectList(null);
            for (Comment comment : commentList) {
                handleComment(comment, commentMapper);
            }
            sqlSession.commit();
            List<Question> questionList = questionMapper.selectList(null);
            for (Question question : questionList) {
                handleQuestion(question, questionMapper);
            }
            sqlSession.commit();
        }
    }

    public static void handleAnswer(Answer answer, AnswerMapper answerMapper) {
        String text = answer.getContent();
        Set<String> retrievedWords = tokenize(text);
        String title = answer.getTitle();
        retrievedWords.addAll(tokenize(title));
        Answer newAnswer = new Answer();
        newAnswer.setId(answer.getId());
        newAnswer.setTokenization(String.join(" | ", retrievedWords));
        answerMapper.updateById(newAnswer);
    }

    public static void handleComment(Comment comment, CommentMapper commentMapper) {
        String text = comment.getContent();
        Set<String> retrievedWords = tokenize(text);
        Comment newComment = new Comment();
        newComment.setId(comment.getId());
        newComment.setTokenization(String.join(" | ", retrievedWords));
        commentMapper.updateById(newComment);
    }

    public static void handleQuestion(Question question, QuestionMapper questionMapper) {
        String text = question.getContent();
        Set<String> retrievedWords = tokenize(text);
        String title = question.getTitle();
        retrievedWords.addAll(tokenize(title));
        Question newQuestion = new Question();
        newQuestion.setId(question.getId());
        newQuestion.setTokenization(String.join(" | ", retrievedWords));
        questionMapper.updateById(newQuestion);
    }

    public static Set<String> tokenize(String text){
        String textWithoutHtml = removeHtmlTagsWithJsoup(text);
        CoreDocument document = new CoreDocument(textWithoutHtml);

        // 注释文本（执行分词）
        pipeline.annotate(document);

        // 获取分词结果
        List<CoreSentence> sentences = document.sentences();
        Set<String> retrievedWords = new HashSet<>();
        for (CoreSentence sentence : sentences) {
            List<String> filteredTokens = sentence.tokens().stream()
                    .map(CoreLabel::word)
                    .filter(token -> !stopwords.contains(token.toLowerCase()))
                    .filter(token -> !token.matches("\\d+"))
                    .filter(token -> !token.matches("\\w"))
                    .filter(token -> !punctuations.contains(token))
                    .toList();
            retrievedWords.addAll(filteredTokens);
        }
        return retrievedWords;
    }

    public static String removeHtmlTagsWithJsoup(String htmlText) {
        // 使用Jsoup清理HTML，允许的标签为空（即不允许任何标签）
        return Jsoup.clean(htmlText, Safelist.none());
    }
}