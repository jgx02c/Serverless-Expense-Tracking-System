resource "aws_dynamodb_table" "this" {
  name           = var.table_name
  billing_mode   = "PROVISIONED"
  read_capacity  = 5
  write_capacity = 5
  hash_key       = var.hash_key

  dynamic "attribute" {
    for_each = var.attributes
    content {
      name = attribute.value.name
      type = attribute.value.type
    }
  }

  dynamic "global_secondary_index" {
    for_each = var.global_secondary_indexes
    content {
      name               = global_secondary_index.value.name
      hash_key           = global_secondary_index.value.hash_key
      range_key          = global_secondary_index.value.range_key
      projection_type    = global_secondary_index.value.projection_type
      read_capacity      = global_secondary_index.value.read_capacity
      write_capacity     = global_secondary_index.value.write_capacity
    }
  }

  tags = merge(
    var.tags,
    {
      Name = var.table_name
    }
  )
}

# Enable point-in-time recovery
resource "aws_dynamodb_table_point_in_time_recovery" "this" {
  table_name = aws_dynamodb_table.this.name
}

# Enable server-side encryption
resource "aws_dynamodb_table_server_side_encryption" "this" {
  table_name = aws_dynamodb_table.this.name
  enabled    = true
} 