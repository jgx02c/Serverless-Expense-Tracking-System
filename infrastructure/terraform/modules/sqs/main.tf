resource "aws_sqs_queue" "this" {
  name = var.queue_name

  visibility_timeout_seconds = 30
  message_retention_seconds = 345600 # 4 days

  # Enable server-side encryption
  sqs_managed_sse_enabled = true

  # Enable dead-letter queue
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })

  # Enable message deduplication
  deduplication_scope = "MESSAGE_GROUP"

  # Enable FIFO queue
  fifo_queue = true
  content_based_deduplication = true

  tags = merge(
    var.tags,
    {
      Name = var.queue_name
    }
  )
}

# Dead Letter Queue
resource "aws_sqs_queue" "dlq" {
  name = "${var.queue_name}-dlq"

  visibility_timeout_seconds = 30
  message_retention_seconds = 1209600 # 14 days

  sqs_managed_sse_enabled = true

  fifo_queue = true
  content_based_deduplication = true

  tags = merge(
    var.tags,
    {
      Name = "${var.queue_name}-dlq"
    }
  )
}

# IAM policy for SQS
resource "aws_sqs_queue_policy" "this" {
  queue_url = aws_sqs_queue.this.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          AWS = var.lambda_role_arn
        }
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.this.arn
      }
    ]
  })
}

# CloudWatch alarm for queue depth
resource "aws_cloudwatch_metric_alarm" "queue_depth" {
  alarm_name          = "${var.queue_name}-depth"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period             = "300"
  statistic          = "Average"
  threshold          = "100"
  alarm_description  = "This metric monitors the number of messages in the queue"
  alarm_actions      = [var.sns_topic_arn]
  ok_actions         = [var.sns_topic_arn]

  dimensions = {
    QueueName = aws_sqs_queue.this.name
  }
} 