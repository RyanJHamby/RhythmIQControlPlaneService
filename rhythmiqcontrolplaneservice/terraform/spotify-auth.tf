resource "aws_ssm_parameter" "spotify_client_id" {
  name  = "/rhythmiq/spotify/client_id"
  type  = "SecureString"
  value = var.spotify_client_id
}

resource "aws_ssm_parameter" "spotify_client_secret" {
  name  = "/rhythmiq/spotify/client_secret"
  type  = "SecureString"
  value = var.spotify_client_secret
}

resource "aws_lambda_function" "spotify_auth" {
  filename         = "../build/libs/spotify-auth.jar"
  function_name    = "spotify-auth"
  role            = aws_iam_role.lambda_role.arn
  handler         = "com.rhythmiq.controlplane.lambda.SpotifyAuthHandler::handleRequest"
  runtime         = "java21"
  timeout         = 30
  memory_size     = 256
  snap_start {
    apply_on = "PublishedVersions"
  }

  environment {
    variables = {
      SPOTIFY_REDIRECT_URI = var.spotify_redirect_uri
    }
  }
}

resource "aws_iam_role_policy" "spotify_auth_policy" {
  name = "spotify-auth-policy"
  role = aws_iam_role.lambda_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter"
        ]
        Resource = [
          aws_ssm_parameter.spotify_client_id.arn,
          aws_ssm_parameter.spotify_client_secret.arn
        ]
      }
    ]
  })
}

resource "aws_apigatewayv2_api" "spotify_auth_api" {
  name          = "spotify-auth-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "spotify_auth_integration" {
  api_id           = aws_apigatewayv2_api.spotify_auth_api.id
  integration_type = "AWS_PROXY"

  integration_method = "POST"
  integration_uri    = aws_lambda_function.spotify_auth.invoke_arn
}

resource "aws_apigatewayv2_route" "spotify_auth_route" {
  api_id    = aws_apigatewayv2_api.spotify_auth_api.id
  route_key = "POST /spotify/token"
  target    = "integrations/${aws_apigatewayv2_integration.spotify_auth_integration.id}"
}

resource "aws_lambda_permission" "spotify_auth_permission" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.spotify_auth.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.spotify_auth_api.execution_arn}/*/*"
}

resource "aws_apigatewayv2_stage" "spotify_auth_stage" {
  api_id = aws_apigatewayv2_api.spotify_auth_api.id
  name   = "prod"
  auto_deploy = true
} 