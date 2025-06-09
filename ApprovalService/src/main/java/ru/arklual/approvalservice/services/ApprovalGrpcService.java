package ru.arklual.approvalservice.services;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import ru.arklual.approvalservice.dto.protobuf.ApprovalServiceGrpc;
import ru.arklual.approvalservice.dto.protobuf.ApprovalServiceProto.*;
import ru.arklual.approvalservice.dto.protobuf.PostProto;

import java.util.Optional;
import java.util.Set;

@GrpcService
public class ApprovalGrpcService extends ApprovalServiceGrpc.ApprovalServiceImplBase {

    private final WaitingPostsRegistry waitingPostsRegistry;
    private final KafkaProducer kafkaProducer;

    public ApprovalGrpcService(WaitingPostsRegistry waitingPostsRegistry, KafkaProducer kafkaProducer) {
        this.waitingPostsRegistry = waitingPostsRegistry;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void approvePost(TeamPostRequest request, StreamObserver<MessageResponse> responseObserver) {
        handleDecision(request, PostProto.PostStatus.APPROVED, "content.%s.approved", responseObserver);
    }

    @Override
    public void rejectPost(TeamPostRequest request, StreamObserver<MessageResponse> responseObserver) {
        handleDecision(request, PostProto.PostStatus.REJECTED, "content.%s.rejected", responseObserver);
    }

    @Override
    public void getPendingPosts(TeamIdRequest request, StreamObserver<PostList> responseObserver) {
        Set<PostProto.Post> posts = waitingPostsRegistry.getPosts(request.getTeamId());

        PostList response = PostList.newBuilder()
                .addAllPosts(posts)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void handleDecision(TeamPostRequest request, PostProto.PostStatus status, String topic,
                                StreamObserver<MessageResponse> responseObserver) {
        Set<PostProto.Post> posts = waitingPostsRegistry.getPosts(request.getTeamId());

        Optional<PostProto.Post> postOpt = posts.stream()
                .filter(p -> p.getId().equals(request.getPostId()))
                .findFirst();

        if (postOpt.isEmpty()) {
            responseObserver.onNext(MessageResponse.newBuilder()
                    .setMessage("Post with id %s not found".formatted(request.getPostId()))
                    .build());
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Post with id %s not found".formatted(request.getPostId()))
                            .asRuntimeException()
            );
            return;
        }

        PostProto.Post post = postOpt.get();
        posts.remove(post);
        PostProto.Post.Builder updated = post.toBuilder().setStatus(status);
        for (PostProto.Sink s: post.getSinkList()) {
            updated.clearSink();
            updated.addSink(s);
            kafkaProducer.sendMessage(String.format(topic, s.getSinkTypeCase()), updated.build());
        }

        responseObserver.onNext(MessageResponse.newBuilder()
                .setMessage("Post with id %s %s".formatted(request.getPostId(), status.name().toLowerCase()))
                .build());
        responseObserver.onCompleted();
    }
}
